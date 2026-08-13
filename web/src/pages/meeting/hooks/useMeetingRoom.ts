import { useCallback, useEffect, useMemo } from "react";
import type { RoomParticipant } from "../types";
import { useMeetingSignaling } from "./useMeetingSignaling";
import { useWebRtcMesh } from "./useWebRtcMesh";

interface UseMeetingRoomOptions {
  meetingId: number | null;
  displayName: string;
  /** JWT에서 꺼낸 본인 memberId. mesh의 offer 방향과 본인 타일 표시에 쓴다. */
  selfMemberId: number | null;
}

/**
 * 회의방 통합 훅.
 * - WebSocket 하나로 signaling(roster·자막)과 WebRTC offer/answer/ice를 함께 나른다.
 * - 미디어는 P2P mesh(useWebRtcMesh). 참가자 목록의 정본은 서버 roster다.
 * - 연결 진행 상태(hasLocalMedia, retryAttempt)와 수동 재시도(manualRetry) 노출
 */
export const useMeetingRoom = ({
  meetingId,
  displayName,
  selfMemberId,
}: UseMeetingRoomOptions) => {
  const signaling = useMeetingSignaling({ meetingId, displayName });

  const peerMemberIds = useMemo(
    () =>
      signaling.participants
        .map((participant) => Number(participant.id))
        .filter((id) => Number.isFinite(id)),
    [signaling.participants],
  );

  const mesh = useWebRtcMesh({
    meetingId,
    selfMemberId,
    peerMemberIds,
    isWsConnected: signaling.isConnected,
    sendSignal: signaling.sendSignal,
    subscribeSignal: signaling.subscribeSignal,
  });

  const participants = useMemo<RoomParticipant[]>(
    () =>
      signaling.participants.map((participant) => ({
        ...participant,
        isSelf: participant.id === String(selfMemberId),
        isSpeaking: mesh.speakingMemberIds.includes(participant.id),
      })),
    [signaling.participants, mesh.speakingMemberIds, selfMemberId],
  );

  const manualRetry = useCallback(() => {
    signaling.manualRetry();
    mesh.manualRetry();
  }, [signaling.manualRetry, mesh.manualRetry]);

  const retryAttempt = Math.max(signaling.retryAttempt, mesh.retryAttempt);

  // 타인이 회의를 종료한 시그널이 오면 peer 연결과 마이크 트랙 즉시 해제
  const meshDisconnect = mesh.disconnect;
  useEffect(() => {
    if (!signaling.isMeetingEnded) return;
    meshDisconnect();
  }, [signaling.isMeetingEnded, meshDisconnect]);

  return {
    participants,
    isWsConnected: signaling.isConnected,
    isRoomConnected: mesh.isConnected,
    hasLocalMedia: mesh.hasLocalMedia,
    retryAttempt,
    // 미디어 에러를 우선 노출, 없으면 signaling 에러
    errorMessage: mesh.errorMessage ?? signaling.errorMessage,
    transcripts: signaling.transcripts,
    interimByMember: signaling.interimByMember,
    sendMessage: signaling.sendMessage,
    isMicEnabled: mesh.isMicEnabled,
    isAudioOutputEnabled: mesh.isAudioOutputEnabled,
    setMicrophoneEnabled: mesh.setMicrophoneEnabled,
    setAudioOutputEnabled: mesh.setAudioOutputEnabled,
    manualRetry,
    isMeetingEnded: signaling.isMeetingEnded,
  };
};
