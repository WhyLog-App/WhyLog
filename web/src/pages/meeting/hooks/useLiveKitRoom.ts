import {
  type RemoteParticipant,
  type RemoteTrack,
  Room,
  RoomEvent,
  Track,
  TrackEvent,
} from "livekit-client";
import { useCallback, useEffect, useRef, useState } from "react";
import { getRtcToken } from "@/apis/meetings";
import type { RoomParticipant as MeetingParticipant } from "../types";

interface UseLiveKitRoomOptions {
  meetingId: number | null;
}

const MAX_RETRIES = 3;
const RETRY_DELAY_MS = 2000;

/**
 * LiveKit SFU(Room) 연결 및 미디어 트랙 제어.
 * - Room 입장 / 퇴장
 * - 참가자 join / leave 이벤트
 * - 마이크 mute/unmute
 * - 원격 audio output volume 제어
 * - RTC 토큰 발급/연결 실패 시 자동 재시도
 */
export const useLiveKitRoom = ({ meetingId }: UseLiveKitRoomOptions) => {
  const [participants, setParticipants] = useState<MeetingParticipant[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [hasRtcToken, setHasRtcToken] = useState(false);
  const [retryAttempt, setRetryAttempt] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isMicEnabled, setIsMicEnabled] = useState(true);
  const [isAudioOutputEnabled, setIsAudioOutputEnabled] = useState(true);
  const [, setRetrySignal] = useState(0);

  const roomRef = useRef<Room | null>(null);
  // 이벤트 콜백/connect 이후 시점에 최신 의도 상태를 참조하기 위한 ref
  const isAudioOutputEnabledRef = useRef(true);
  const isMicEnabledRef = useRef(true);

  const setMicrophoneEnabled = useCallback(async (enabled: boolean) => {
    isMicEnabledRef.current = enabled;
    const room = roomRef.current;
    if (!room) {
      setIsMicEnabled(enabled);
      return;
    }
    try {
      await room.localParticipant.setMicrophoneEnabled(enabled);
      setIsMicEnabled(enabled);
    } catch {
      /* mic 권한 거부 등 실패 시 상태 변경 안 함 */
    }
  }, []);

  const setAudioOutputEnabled = useCallback((enabled: boolean) => {
    isAudioOutputEnabledRef.current = enabled;
    setIsAudioOutputEnabled(enabled);
    const room = roomRef.current;
    if (!room) return;
    room.remoteParticipants.forEach((p) => {
      p.audioTrackPublications.forEach((pub) => {
        const track = pub.track;
        if (!track) return;
        track.attachedElements.forEach((el) => {
          el.volume = enabled ? 1 : 0;
        });
      });
    });
  }, []);

  const disconnect = useCallback(async () => {
    const room = roomRef.current;
    isMicEnabledRef.current = false;
    setIsMicEnabled(false);
    if (!room) {
      setIsConnected(false);
      return;
    }
    try {
      await room.localParticipant.setMicrophoneEnabled(false);
    } catch {
      /* noop */
    }
    try {
      await room.disconnect();
    } catch {
      /* noop */
    }
    setIsConnected(false);
  }, []);

  const manualRetry = useCallback(() => {
    setErrorMessage(null);
    setRetryAttempt(0);
    setHasRtcToken(false);
    setIsConnected(false);
    setRetrySignal((n) => n + 1);
  }, []);

  useEffect(() => {
    if (meetingId == null) return;

    let cancelled = false;
    const room = new Room();
    roomRef.current = room;

    room.on(RoomEvent.ParticipantConnected, (p: RemoteParticipant) => {
      setParticipants((prev) => {
        if (prev.some((x) => x.id === p.identity)) return prev;
        return [...prev, { id: p.identity, name: p.name || p.identity }];
      });
    });
    room.on(RoomEvent.ParticipantDisconnected, (p: RemoteParticipant) => {
      setParticipants((prev) => prev.filter((x) => x.id !== p.identity));
    });

    // 원격 audio track 을 DOM 에 attach 해 실제 재생되도록 하고, 현재 듣기 상태 적용
    const attachAudioTrack = (track: RemoteTrack) => {
      if (track.kind !== Track.Kind.Audio) return;
      const apply = (el: HTMLMediaElement) => {
        el.volume = isAudioOutputEnabledRef.current ? 1 : 0;
      };
      if (track.attachedElements.length === 0) {
        const el = track.attach();
        el.style.display = "none";
        document.body.appendChild(el);
      }
      track.attachedElements.forEach(apply);
      track.on(TrackEvent.ElementAttached, apply);
    };

    room.on(RoomEvent.TrackSubscribed, (track: RemoteTrack) => {
      attachAudioTrack(track);
    });

    // 트랙 구독 해제 시 audio element 정리 + 리스너 제거
    room.on(RoomEvent.TrackUnsubscribed, (track: RemoteTrack) => {
      if (track.kind !== Track.Kind.Audio) return;
      track.detach().forEach((el) => {
        el.remove();
      });
      track.removeAllListeners(TrackEvent.ElementAttached);
    });

    let attempt = 0;
    const tryConnect = async (): Promise<void> => {
      try {
        const { serverUrl, token } = await getRtcToken(meetingId);
        if (cancelled) return;
        setHasRtcToken(true);

        await room.connect(serverUrl, token);
        if (cancelled) {
          await room.disconnect();
          return;
        }

        // 이미 입장해 있던 참가자/오디오 트랙 시드 (후발 입장 시나리오)
        const initial: MeetingParticipant[] = [];
        room.remoteParticipants.forEach((p) => {
          initial.push({ id: p.identity, name: p.name || p.identity });
          p.audioTrackPublications.forEach((pub) => {
            if (pub.track) attachAudioTrack(pub.track);
          });
        });
        if (initial.length > 0) {
          setParticipants((prev) => {
            const merged = [...prev];
            initial.forEach((p) => {
              if (!merged.some((x) => x.id === p.id)) merged.push(p);
            });
            return merged;
          });
        }

        setIsConnected(true);
        setErrorMessage(null);
        setRetryAttempt(0);
        // 사용자가 직전에 mute 했어도 그 의도를 보존
        await room.localParticipant.setMicrophoneEnabled(
          isMicEnabledRef.current,
        );
      } catch (err) {
        if (cancelled) return;
        attempt += 1;
        setRetryAttempt(attempt);

        if (attempt < MAX_RETRIES) {
          // backoff 후 재시도
          await new Promise((r) => setTimeout(r, RETRY_DELAY_MS));
          if (cancelled) return;
          try {
            await room.disconnect();
          } catch {
            /* noop */
          }
          return tryConnect();
        }

        setErrorMessage(
          err instanceof Error ? err.message : "회의방 연결 실패",
        );
      }
    };
    tryConnect();

    return () => {
      cancelled = true;
      room.disconnect().catch(() => {
        /* noop */
      });
      roomRef.current = null;
      setIsConnected(false);
      setHasRtcToken(false);
      setParticipants([]);
      // 이전 회의 에러가 새 화면에 잔존하지 않도록 초기화
      setErrorMessage(null);
    };
  }, [meetingId]);

  return {
    participants,
    isConnected,
    hasRtcToken,
    retryAttempt,
    errorMessage,
    isMicEnabled,
    isAudioOutputEnabled,
    setMicrophoneEnabled,
    setAudioOutputEnabled,
    manualRetry,
    disconnect,
  };
};
