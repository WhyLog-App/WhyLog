import { useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import Modal from "@/components/common/Modal";
import { decodeAccessToken } from "@/utils/jwt";
import { parseRouteId } from "@/utils/parseRouteId";
import { tokenStore } from "@/utils/tokenStore";
import LiveTranscriptPanel from "./components/LiveTranscriptPanel";
import MeetingConnectionOverlay from "./components/MeetingConnectionOverlay";
import MeetingControls from "./components/MeetingControls";
import MeetingHeader from "./components/MeetingHeader";
import ParticipantGrid from "./components/ParticipantGrid";
import { useElapsedTime } from "./hooks/useElapsedTime";
import { useEndMeeting } from "./hooks/useEndMeeting";
import { useLocalSpeechFallback } from "./hooks/useLocalSpeechFallback";
import { useMeetingDetail } from "./hooks/useMeetingDetail";
import { useMeetingRoom } from "./hooks/useMeetingRoom";
import { useSpeechRecognition } from "./hooks/useSpeechRecognition";

const formatStartDateTime = (iso: string) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const parseStartTimestamp = (iso: string | undefined): number | undefined => {
  if (!iso) return undefined;
  const t = new Date(iso).getTime();
  return Number.isNaN(t) ? undefined : t;
};

const InProgressPage = () => {
  const { meetingId: meetingIdParam } = useParams<{ meetingId: string }>();
  const location = useLocation();
  const navigate = useNavigate();

  const meetingId = parseRouteId(meetingIdParam);
  const { data: meetingDetail } = useMeetingDetail(meetingId);

  const meetingName =
    meetingDetail?.name ??
    (location.state as { name?: string } | null)?.name ??
    "회의";
  const meetingStart = meetingDetail?.start_date_time
    ? formatStartDateTime(meetingDetail.start_date_time)
    : null;
  const memberCount = meetingDetail?.member_count ?? 0;

  const startedAt = parseStartTimestamp(meetingDetail?.start_date_time);
  const elapsed = useElapsedTime(startedAt);

  const accessToken = tokenStore.getToken();
  const myMemberId = accessToken
    ? (decodeAccessToken(accessToken)?.memberId ?? null)
    : null;
  const myName =
    meetingDetail?.members.find((m) => m.member_id === myMemberId)?.name ?? "";

  const {
    participants,
    isWsConnected,
    isRoomConnected,
    hasRtcToken,
    retryAttempt,
    errorMessage,
    transcripts,
    interimByMember,
    sendMessage,
    isMicEnabled,
    isAudioOutputEnabled,
    setMicrophoneEnabled,
    setAudioOutputEnabled,
    manualRetry,
    isMeetingEnded,
  } = useMeetingRoom({
    meetingId,
    displayName: myName,
  });

  const MAX_CONNECTION_RETRIES = 3;
  const isConnecting = !isMeetingEnded && (!isWsConnected || !isRoomConnected);

  const { handleInterim, handleFinal, mergeTranscripts, mergeInterim } =
    useLocalSpeechFallback({
      isWsConnected,
      isMicEnabled,
      sendMessage,
    });

  const { isSupported: isSpeechSupported } = useSpeechRecognition({
    enabled: isMicEnabled, // 마이크 음소거 시 음성 인식도 정지
    onInterim: handleInterim,
    onFinal: handleFinal,
  });

  const {
    endMeeting,
    isPending: isEnding,
    errorMessage: endErrorMessage,
  } = useEndMeeting();
  const [isEndConfirmOpen, setIsEndConfirmOpen] = useState(false);

  const profileImageByMemberId = useMemo(() => {
    const map = new Map<string, string | null>();
    meetingDetail?.members.forEach((m) => {
      map.set(String(m.member_id), m.profile_image);
    });
    return map;
  }, [meetingDetail?.members]);

  const profileImageByName = useMemo(() => {
    const map = new Map<string, string | null>();
    meetingDetail?.members.forEach((m) => {
      if (m.name) map.set(m.name, m.profile_image);
    });
    return map;
  }, [meetingDetail?.members]);

  if (meetingId == null) {
    return null;
  }

  const baseParticipants =
    participants.length > 0
      ? participants
      : myName && myMemberId != null
        ? [{ id: String(myMemberId), name: myName, isSelf: true }]
        : [];

  const displayParticipants = baseParticipants.map((p) => ({
    ...p,
    profileImage:
      profileImageByMemberId.get(p.id) ??
      profileImageByName.get(p.name) ??
      null,
  }));

  const handleEndClick = () => {
    if (meetingId == null) {
      navigate("/");
      return;
    }
    setIsEndConfirmOpen(true);
  };

  const handleEndConfirm = () => {
    if (meetingId != null) endMeeting(meetingId);
    setIsEndConfirmOpen(false);
  };

  return (
    <div className="flex h-full flex-col py-6 lg:py-10">
      {isConnecting && (
        <MeetingConnectionOverlay
          isWsConnected={isWsConnected}
          isRoomConnected={isRoomConnected}
          hasRtcToken={hasRtcToken}
          errorMessage={errorMessage}
          retryAttempt={retryAttempt}
          maxRetries={MAX_CONNECTION_RETRIES}
          onManualRetry={manualRetry}
          onExit={() => navigate("/")}
        />
      )}
      <MeetingHeader
        meetingName={meetingName}
        meetingStart={meetingStart}
        memberCount={memberCount}
        elapsed={elapsed}
        isWsConnected={isWsConnected}
        isRoomConnected={isRoomConnected}
        participants={displayParticipants}
      />

      {!isConnecting && (errorMessage || endErrorMessage) && (
        <div className="mt-3 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {errorMessage ?? endErrorMessage}
        </div>
      )}

      <div className="mt-6 flex min-h-0 flex-1 flex-col gap-6 overflow-y-auto lg:mt-10 lg:flex-row lg:overflow-hidden">
        <div className="flex shrink-0 flex-col items-center justify-center lg:flex-1">
          <ParticipantGrid participants={displayParticipants} />
        </div>
        <LiveTranscriptPanel
          transcripts={mergeTranscripts(transcripts)}
          interimByMember={mergeInterim(interimByMember)}
          isSupported={isSpeechSupported}
        />
      </div>

      <div className="mt-6 flex justify-center">
        <MeetingControls
          onEnd={handleEndClick}
          isMicEnabled={isMicEnabled}
          isAudioOutputEnabled={isAudioOutputEnabled}
          onToggleMic={() => setMicrophoneEnabled(!isMicEnabled)}
          onToggleAudioOutput={() =>
            setAudioOutputEnabled(!isAudioOutputEnabled)
          }
        />
      </div>

      {isMeetingEnded && (
        <Modal
          title="회의가 종료되었어요"
          onClose={() => navigate("/")}
          primaryLabel="홈으로"
          onPrimaryClick={() => navigate("/")}
        >
          <p className="typo-body-md text-(--color-text-secondary)">
            다른 참가자가 회의를 종료했어요. 분석이 곧 시작됩니다.
          </p>
        </Modal>
      )}

      {isEndConfirmOpen && !isMeetingEnded && (
        <Modal
          title="회의를 종료하시겠어요?"
          onClose={() => setIsEndConfirmOpen(false)}
          primaryLabel="종료"
          onPrimaryClick={handleEndConfirm}
          isPrimaryDisabled={isEnding}
        >
          <p className="typo-body-md text-(--color-text-secondary)">
            종료 시 회의가 즉시 마감되며 분석이 시작됩니다.
          </p>
        </Modal>
      )}
    </div>
  );
};

export default InProgressPage;
