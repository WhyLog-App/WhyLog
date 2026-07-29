import EmptyStateIllustrationUrl from "@/assets/illustrations/empty_state_illustration.svg";

interface MeetingConnectionOverlayProps {
  isWsConnected: boolean;
  isRoomConnected: boolean;
  hasRtcToken: boolean;
  errorMessage: string | null;
  retryAttempt: number;
  maxRetries: number;
  onManualRetry: () => void;
  onExit?: () => void;
}

const getStatusText = ({
  isWsConnected,
  isRoomConnected,
  hasRtcToken,
  errorMessage,
  retryAttempt,
  maxRetries,
}: Omit<MeetingConnectionOverlayProps, "onManualRetry" | "onExit">): string => {
  if (errorMessage && retryAttempt >= maxRetries) {
    return "연결에 실패했습니다";
  }
  if (errorMessage) {
    return `연결 재시도 중... (${retryAttempt}/${maxRetries})`;
  }
  if (!hasRtcToken) {
    return "회의 토큰 발급 중...";
  }
  if (!isRoomConnected) {
    return "회의실에 입장하는 중...";
  }
  if (!isWsConnected) {
    return "실시간 채널 연결 중...";
  }
  return "연결 중...";
};

const MeetingConnectionOverlay = ({
  isWsConnected,
  isRoomConnected,
  hasRtcToken,
  errorMessage,
  retryAttempt,
  maxRetries,
  onManualRetry,
  onExit,
}: MeetingConnectionOverlayProps) => {
  const statusText = getStatusText({
    isWsConnected,
    isRoomConnected,
    hasRtcToken,
    errorMessage,
    retryAttempt,
    maxRetries,
  });
  const isFailed = errorMessage != null && retryAttempt >= maxRetries;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-(--color-overlay-dim)"
      role="status"
      aria-live="polite"
    >
      <div className="flex flex-col items-center gap-7">
        <img
          src={EmptyStateIllustrationUrl}
          alt=""
          aria-hidden="true"
          width={160}
          height={160}
        />
        <div className="flex flex-col items-center gap-2 text-center">
          <p className="typo-h6 text-(--color-text-inverse)">{statusText}</p>
          {isFailed && errorMessage && (
            <p className="typo-caption1 text-(--color-text-inverse) opacity-70">
              {errorMessage}
            </p>
          )}
        </div>

        {isFailed && (
          <div className="flex gap-3">
            {onExit && (
              <button
                type="button"
                className="typo-button-md flex cursor-pointer items-center justify-center rounded-xl border-[1.5px] border-white/40 bg-white/10 px-7 py-3 text-(--color-text-inverse) hover:bg-white/20"
                onClick={onExit}
              >
                나가기
              </button>
            )}
            <button
              type="button"
              className="typo-button-md flex cursor-pointer items-center justify-center rounded-xl bg-linear-to-br from-[#5B8DEF] to-[#0063F7] px-7 py-3 text-(--color-text-inverse) shadow-[0px_4px_12px_0px_rgba(30,91,232,0.2)]"
              onClick={onManualRetry}
            >
              다시 시도
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default MeetingConnectionOverlay;
