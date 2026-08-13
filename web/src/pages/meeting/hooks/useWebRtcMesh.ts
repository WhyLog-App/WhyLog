import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import {
  MAX_MEDIA_RETRIES,
  MEDIA_RETRY_DELAY_MS,
  RTC_CONFIG,
  SPEAKING_HOLD_MS,
  SPEAKING_RMS_THRESHOLD,
  SPEAKING_SAMPLE_INTERVAL_MS,
} from "../constants";
import type { SignalFrame, SignalMessageType, SignalPayload } from "../types";

interface UseWebRtcMeshOptions {
  meetingId: number | null;
  /** JWT에서 꺼낸 본인 memberId. offer 방향 결정에 쓰이므로 없으면 mesh를 시작하지 않는다. */
  selfMemberId: number | null;
  /** roster 기준 참가자 memberId 목록 (본인 포함 가능, 내부에서 제외한다) */
  peerMemberIds: number[];
  isWsConnected: boolean;
  sendSignal: (
    type: SignalMessageType,
    targetMemberId: number,
    payload: SignalPayload,
  ) => boolean;
  subscribeSignal: (handler: (frame: SignalFrame) => void) => () => void;
}

interface PeerEntry {
  connection: RTCPeerConnection;
  audioElement: HTMLAudioElement | null;
  analyser: AnalyserNode | null;
}

/**
 * WebSocket 시그널링 위에 올린 P2P WebRTC mesh (오디오 전용).
 * - LiveKit SFU를 대체한다. 서버는 offer/answer/ice를 1:1 중계만 한다.
 * - offer는 memberId가 작은 쪽이 건다 (glare 방지). 재협상은 하지 않는다.
 * - 로컬/원격 스트림 볼륨을 재서 발화 중인 참가자를 표시한다.
 */
export const useWebRtcMesh = ({
  meetingId,
  selfMemberId,
  peerMemberIds,
  isWsConnected,
  sendSignal,
  subscribeSignal,
}: UseWebRtcMeshOptions) => {
  const [hasLocalMedia, setHasLocalMedia] = useState(false);
  const [retryAttempt, setRetryAttempt] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isMicEnabled, setIsMicEnabled] = useState(true);
  const [isAudioOutputEnabled, setIsAudioOutputEnabled] = useState(true);
  const [speakingMemberIds, setSpeakingMemberIds] = useState<string[]>([]);
  const [retrySignal, setRetrySignal] = useState(0);

  const localStreamRef = useRef<MediaStream | null>(null);
  const peersRef = useRef<Map<number, PeerEntry>>(new Map());
  // setRemoteDescription 전에 도착한 ICE 후보를 peer별로 모아둔다.
  const pendingIceRef = useRef<Map<number, RTCIceCandidateInit[]>>(new Map());
  // 마이크 확보 전에 도착한 offer/ice. 버리면 상대가 재시도하지 않아 연결이 영영 안 선다.
  const pendingSignalsRef = useRef<SignalFrame[]>([]);
  const audioContextRef = useRef<AudioContext | null>(null);
  const localAnalyserRef = useRef<AnalyserNode | null>(null);
  const isMicEnabledRef = useRef(true);
  const isAudioOutputEnabledRef = useRef(true);
  const hasLocalMediaRef = useRef(false);

  // 이벤트 콜백에서 최신 값을 읽되, 값이 바뀌어도 연결을 다시 세우지 않도록 ref로 잡아둔다.
  const sendSignalRef = useRef(sendSignal);
  sendSignalRef.current = sendSignal;
  const selfMemberIdRef = useRef(selfMemberId);
  selfMemberIdRef.current = selfMemberId;

  const setMicrophoneEnabled = useCallback((enabled: boolean) => {
    isMicEnabledRef.current = enabled;
    setIsMicEnabled(enabled);
    // 트랙을 제거하지 않고 enabled만 끈다. 제거하면 재협상이 필요해진다.
    localStreamRef.current?.getAudioTracks().forEach((track) => {
      track.enabled = enabled;
    });
  }, []);

  const setAudioOutputEnabled = useCallback((enabled: boolean) => {
    isAudioOutputEnabledRef.current = enabled;
    setIsAudioOutputEnabled(enabled);
    peersRef.current.forEach((peer) => {
      if (peer.audioElement) peer.audioElement.volume = enabled ? 1 : 0;
    });
  }, []);

  const closePeer = useCallback((memberId: number) => {
    const peer = peersRef.current.get(memberId);
    if (!peer) return;

    peer.connection.onicecandidate = null;
    peer.connection.ontrack = null;
    peer.connection.onconnectionstatechange = null;
    try {
      peer.connection.close();
    } catch {
      /* 이미 닫힌 연결 */
    }
    peer.analyser?.disconnect();
    if (peer.audioElement) {
      peer.audioElement.srcObject = null;
      peer.audioElement.remove();
    }
    peersRef.current.delete(memberId);
    pendingIceRef.current.delete(memberId);
    setSpeakingMemberIds((prev) =>
      prev.filter((id) => id !== String(memberId)),
    );
  }, []);

  /**
   * peer 연결과 로컬 트랙을 모두 정리한다.
   * 마이크 on/off는 사용자 의도라 여기서 건드리지 않는다 (재연결 후에도 유지돼야 함).
   */
  const disconnect = useCallback(() => {
    Array.from(peersRef.current.keys()).forEach(closePeer);
    pendingSignalsRef.current = [];

    localStreamRef.current?.getTracks().forEach((track) => {
      track.stop();
    });
    localStreamRef.current = null;
    localAnalyserRef.current?.disconnect();
    localAnalyserRef.current = null;
    hasLocalMediaRef.current = false;
    setHasLocalMedia(false);
  }, [closePeer]);

  const manualRetry = useCallback(() => {
    setErrorMessage(null);
    setRetryAttempt(0);
    setRetrySignal((n) => n + 1);
  }, []);

  /** 원격 스트림을 숨은 audio 요소에 붙여 재생하고, 발화 감지를 위한 analyser를 건다. */
  const attachRemoteStream = useCallback(
    (memberId: number, stream: MediaStream) => {
      const peer = peersRef.current.get(memberId);
      if (!peer) return;

      if (!peer.audioElement) {
        const element = document.createElement("audio");
        element.autoplay = true;
        element.style.display = "none";
        document.body.appendChild(element);
        peer.audioElement = element;
      }
      peer.audioElement.srcObject = stream;
      peer.audioElement.volume = isAudioOutputEnabledRef.current ? 1 : 0;
      peer.audioElement.play().catch((error: unknown) => {
        // 자동재생 차단은 사용자 제스처 이후 복구되므로 연결 실패로 취급하지 않는다.
        console.error(`원격 오디오 자동재생 실패: memberId=${memberId}`, error);
      });

      const context = audioContextRef.current;
      if (context && !peer.analyser) {
        try {
          const analyser = context.createAnalyser();
          analyser.fftSize = 512;
          context.createMediaStreamSource(stream).connect(analyser);
          peer.analyser = analyser;
        } catch (error: unknown) {
          // analyser 실패는 발화 표시만 못 하는 것이라 통화 자체는 계속 간다.
          console.error(`발화 감지 연결 실패: memberId=${memberId}`, error);
        }
      }
    },
    [],
  );

  const createPeer = useCallback(
    (memberId: number): PeerEntry | null => {
      const localStream = localStreamRef.current;
      if (!localStream) return null;

      const connection = new RTCPeerConnection(RTC_CONFIG);
      const peer: PeerEntry = {
        connection,
        audioElement: null,
        analyser: null,
      };
      peersRef.current.set(memberId, peer);

      localStream.getTracks().forEach((track) => {
        connection.addTrack(track, localStream);
      });

      connection.onicecandidate = (event) => {
        if (!event.candidate) return;
        sendSignalRef.current("ice", memberId, {
          candidate: event.candidate.toJSON(),
        });
      };

      connection.ontrack = (event) => {
        const [stream] = event.streams;
        if (stream) attachRemoteStream(memberId, stream);
      };

      connection.onconnectionstatechange = () => {
        const state = connection.connectionState;
        if (state === "failed" || state === "closed") {
          console.error(`peer 연결 종료: memberId=${memberId}, state=${state}`);
        }
      };

      return peer;
    },
    [attachRemoteStream],
  );

  /** 버퍼링해 둔 ICE 후보를 remote description 설정 후에 흘려보낸다. */
  const flushPendingIce = useCallback(async (memberId: number) => {
    const peer = peersRef.current.get(memberId);
    const pending = pendingIceRef.current.get(memberId);
    if (!peer || !pending) return;

    pendingIceRef.current.delete(memberId);
    for (const candidate of pending) {
      try {
        await peer.connection.addIceCandidate(candidate);
      } catch (error: unknown) {
        console.error(`보류 ICE 추가 실패: memberId=${memberId}`, error);
      }
    }
  }, []);

  const processSignal = useCallback(
    async (frame: SignalFrame) => {
      const { fromMemberId, payload } = frame;
      try {
        if (frame.type === "offer") {
          if (!payload.sdp) return;

          // 상대가 새로고침 후 재입장하면 협상이 끝난 연결에 새 offer가 온다.
          // roster의 퇴장 처리보다 먼저 도착할 수 있어, 여기서도 낡은 연결을 걷어낸다.
          const existing = peersRef.current.get(fromMemberId);
          if (existing?.connection.remoteDescription) {
            closePeer(fromMemberId);
          }

          // offer를 받았다는 건 상대가 initiator라는 뜻이므로 없으면 여기서 만든다.
          const peer =
            peersRef.current.get(fromMemberId) ?? createPeer(fromMemberId);
          if (!peer) return;

          await peer.connection.setRemoteDescription(payload.sdp);
          await flushPendingIce(fromMemberId);
          const answer = await peer.connection.createAnswer();
          await peer.connection.setLocalDescription(answer);
          sendSignalRef.current("answer", fromMemberId, { sdp: answer });
          return;
        }

        if (frame.type === "answer") {
          const peer = peersRef.current.get(fromMemberId);
          if (!peer || !payload.sdp) return;
          // 이미 stable이면 중복 answer이므로 흘린다.
          if (peer.connection.signalingState !== "have-local-offer") return;
          await peer.connection.setRemoteDescription(payload.sdp);
          await flushPendingIce(fromMemberId);
          return;
        }

        if (frame.type === "ice") {
          if (!payload.candidate) return;
          const peer = peersRef.current.get(fromMemberId);
          if (!peer || !peer.connection.remoteDescription) {
            const pending = pendingIceRef.current.get(fromMemberId) ?? [];
            pending.push(payload.candidate);
            pendingIceRef.current.set(fromMemberId, pending);
            return;
          }
          await peer.connection.addIceCandidate(payload.candidate);
        }
      } catch (error: unknown) {
        console.error(
          `시그널링 처리 실패: type=${frame.type}, from=${fromMemberId}`,
          error,
        );
      }
    },
    [createPeer, flushPendingIce, closePeer],
  );

  // 1) 로컬 마이크 확보 + 오디오 컨텍스트 준비
  // biome-ignore lint/correctness/useExhaustiveDependencies: retrySignal은 manualRetry 트리거로 사용
  useEffect(() => {
    if (meetingId == null || selfMemberId == null) return;

    let cancelled = false;
    let attempt = 0;
    let retryTimer: ReturnType<typeof setTimeout> | null = null;

    const acquire = async (): Promise<void> => {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });
        if (cancelled) {
          stream.getTracks().forEach((track) => {
            track.stop();
          });
          return;
        }

        stream.getAudioTracks().forEach((track) => {
          track.enabled = isMicEnabledRef.current;
        });
        localStreamRef.current = stream;

        try {
          const context = new AudioContext();
          audioContextRef.current = context;
          // 자동재생 정책으로 suspended 상태일 수 있다. 실패해도 통화에는 영향이 없다.
          await context.resume().catch(() => undefined);
          const analyser = context.createAnalyser();
          analyser.fftSize = 512;
          context.createMediaStreamSource(stream).connect(analyser);
          localAnalyserRef.current = analyser;
        } catch (error: unknown) {
          console.error("발화 감지용 AudioContext 생성 실패", error);
        }

        hasLocalMediaRef.current = true;
        setHasLocalMedia(true);
        setErrorMessage(null);
        setRetryAttempt(0);
      } catch (error: unknown) {
        if (cancelled) return;
        attempt += 1;
        setRetryAttempt(attempt);
        console.error(
          `마이크 사용 실패 (${attempt}/${MAX_MEDIA_RETRIES})`,
          error,
        );

        if (attempt < MAX_MEDIA_RETRIES) {
          retryTimer = setTimeout(() => {
            if (!cancelled) void acquire();
          }, MEDIA_RETRY_DELAY_MS);
          return;
        }
        setErrorMessage(
          error instanceof Error && error.name === "NotAllowedError"
            ? "마이크 권한이 필요합니다."
            : "마이크를 사용할 수 없습니다.",
        );
      }
    };

    void acquire();

    return () => {
      cancelled = true;
      if (retryTimer) clearTimeout(retryTimer);
      disconnect();
      audioContextRef.current?.close().catch(() => undefined);
      audioContextRef.current = null;
      setErrorMessage(null);
      setRetryAttempt(0);
    };
  }, [meetingId, selfMemberId, retrySignal, disconnect]);

  // 2) 시그널링 수신. 마이크가 아직이면 버퍼에 쌓아 두고 준비되면 처리한다.
  useEffect(() => {
    if (meetingId == null || selfMemberId == null) return;

    return subscribeSignal((frame) => {
      // 본인이 보낸 프레임이 되돌아오는 경우 방어
      if (frame.fromMemberId === selfMemberIdRef.current) return;
      if (!hasLocalMediaRef.current) {
        pendingSignalsRef.current.push(frame);
        return;
      }
      void processSignal(frame);
    });
  }, [meetingId, selfMemberId, subscribeSignal, processSignal]);

  // 3) 마이크가 준비되면 밀린 시그널을 순서대로 흘려보낸다
  useEffect(() => {
    if (!hasLocalMedia) return;
    const buffered = pendingSignalsRef.current;
    if (buffered.length === 0) return;
    pendingSignalsRef.current = [];

    void (async () => {
      for (const frame of buffered) {
        await processSignal(frame);
      }
    })();
  }, [hasLocalMedia, processSignal]);

  // roster 배열은 갱신마다 새 참조라 정렬한 키로 좁혀서 effect 실행을 줄인다.
  const peerKey = useMemo(
    () => [...peerMemberIds].sort((a, b) => a - b).join(","),
    [peerMemberIds],
  );

  // 4) roster 변화에 맞춰 peer 연결을 만들고 지운다
  useEffect(() => {
    if (!hasLocalMedia || !isWsConnected) return;
    const selfId = selfMemberIdRef.current;
    if (selfId == null) return;

    const currentIds = peerKey
      .split(",")
      .filter(Boolean)
      .map(Number)
      .filter((id) => id !== selfId);

    Array.from(peersRef.current.keys()).forEach((existingId) => {
      if (!currentIds.includes(existingId)) closePeer(existingId);
    });

    currentIds.forEach((peerId) => {
      if (peersRef.current.has(peerId)) return;
      const peer = createPeer(peerId);
      if (!peer) return;

      // 양쪽이 동시에 offer를 걸지 않도록 id가 작은 쪽만 initiator가 된다.
      if (selfId >= peerId) return;

      void (async () => {
        try {
          const offer = await peer.connection.createOffer();
          await peer.connection.setLocalDescription(offer);
          sendSignalRef.current("offer", peerId, { sdp: offer });
        } catch (error: unknown) {
          console.error(`offer 생성 실패: memberId=${peerId}`, error);
        }
      })();
    });
  }, [peerKey, hasLocalMedia, isWsConnected, createPeer, closePeer]);

  // 5) 발화 감지 — rAF 대신 저빈도 샘플링으로 리렌더를 억제한다
  useEffect(() => {
    if (!hasLocalMedia) return;

    const buffer = new Uint8Array(256);
    const lastSpokeAt = new Map<string, number>();

    const readRms = (analyser: AnalyserNode): number => {
      analyser.getByteTimeDomainData(buffer);
      let sumSquares = 0;
      for (let i = 0; i < buffer.length; i += 1) {
        const normalized = (buffer[i] - 128) / 128;
        sumSquares += normalized * normalized;
      }
      return Math.sqrt(sumSquares / buffer.length);
    };

    const timer = setInterval(() => {
      const now = Date.now();
      const selfId = selfMemberIdRef.current;

      if (localAnalyserRef.current && selfId != null) {
        // 음소거 중에는 트랙이 무음이라 자연히 임계값 아래로 떨어진다.
        if (readRms(localAnalyserRef.current) >= SPEAKING_RMS_THRESHOLD) {
          lastSpokeAt.set(String(selfId), now);
        }
      }

      peersRef.current.forEach((peer, memberId) => {
        if (!peer.analyser) return;
        if (readRms(peer.analyser) >= SPEAKING_RMS_THRESHOLD) {
          lastSpokeAt.set(String(memberId), now);
        }
      });

      const speaking = Array.from(lastSpokeAt.entries())
        .filter(([, at]) => now - at <= SPEAKING_HOLD_MS)
        .map(([id]) => id)
        .sort();

      // 값이 같으면 setState를 건너뛴다 (100ms마다 리렌더되는 것 방지)
      setSpeakingMemberIds((prev) =>
        prev.length === speaking.length &&
        prev.every((id, index) => id === speaking[index])
          ? prev
          : speaking,
      );
    }, SPEAKING_SAMPLE_INTERVAL_MS);

    return () => {
      clearInterval(timer);
    };
  }, [hasLocalMedia]);

  return {
    hasLocalMedia,
    /** mesh는 SFU가 없어 "입장 완료" 기준이 로컬 미디어 확보다. peer 개별 실패는 화면을 막지 않는다. */
    isConnected: hasLocalMedia,
    speakingMemberIds,
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
