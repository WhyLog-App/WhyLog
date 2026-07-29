import { useCallback, useEffect, useMemo } from "react";
import type { RoomParticipant } from "../types";
import { useLiveKitRoom } from "./useLiveKitRoom";
import { useMeetingSignaling } from "./useMeetingSignaling";

interface UseMeetingRoomOptions {
  meetingId: number | null;
  displayName: string;
}

/**
 * 회의방 통합 훅.
 * - WebSocket(signaling, transcripts) + LiveKit(SFU, 미디어) 두 채널을 묶어
 *   하나의 인터페이스로 노출한다.
 * - 참가자 목록은 두 소스를 병합 (id 기준 중복 제거)
 * - 연결 진행 상태(hasRtcToken, retryAttempt)와 수동 재시도(manualRetry) 노출
 */
export const useMeetingRoom = ({
  meetingId,
  displayName,
}: UseMeetingRoomOptions) => {
  const signaling = useMeetingSignaling({ meetingId, displayName });
  const livekit = useLiveKitRoom({ meetingId });

  const participants = useMemo<RoomParticipant[]>(() => {
    const map = new Map<string, RoomParticipant>();
    [...signaling.participants, ...livekit.participants].forEach((p) => {
      if (!map.has(p.id)) map.set(p.id, p);
    });
    return Array.from(map.values());
  }, [signaling.participants, livekit.participants]);

  const manualRetry = useCallback(() => {
    signaling.manualRetry();
    livekit.manualRetry();
  }, [signaling.manualRetry, livekit.manualRetry]);

  const retryAttempt = Math.max(signaling.retryAttempt, livekit.retryAttempt);

  // 타인이 회의를 종료한 시그널이 오면 LiveKit Room/마이크 즉시 해제
  useEffect(() => {
    if (!signaling.isMeetingEnded) return;
    void livekit.isConnected;
    void livekit.disconnect();
  }, [signaling.isMeetingEnded, livekit.isConnected, livekit.disconnect]);

  return {
    participants,
    isWsConnected: signaling.isConnected,
    isRoomConnected: livekit.isConnected,
    hasRtcToken: livekit.hasRtcToken,
    retryAttempt,
    // LiveKit 에러를 우선 노출, 없으면 signaling 에러
    errorMessage: livekit.errorMessage ?? signaling.errorMessage,
    transcripts: signaling.transcripts,
    interimByMember: signaling.interimByMember,
    sendMessage: signaling.sendMessage,
    isMicEnabled: livekit.isMicEnabled,
    isAudioOutputEnabled: livekit.isAudioOutputEnabled,
    setMicrophoneEnabled: livekit.setMicrophoneEnabled,
    setAudioOutputEnabled: livekit.setAudioOutputEnabled,
    manualRetry,
    isMeetingEnded: signaling.isMeetingEnded,
  };
};
