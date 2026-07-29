import IconClock from "@/assets/icons/media/ic_clock.svg?react";
import { Icon } from "@/components/common/Icon";
import { useLiveElapsedTime } from "@/pages/meeting/hooks/useLiveElapsedTime";

interface MeetingPanelItemProps {
  title: string;
  isActive?: boolean;
  isLive?: boolean;
  elapsedTime?: string;
  date?: string;
  badgeCount?: number;
  onClick?: () => void;
}

interface LiveMeetingPanelItemProps {
  title: string;
  isActive: boolean;
  elapsedTime?: string;
  onClick?: () => void;
}

const LiveMeetingPanelItem = ({
  title,
  isActive,
  elapsedTime,
  onClick,
}: LiveMeetingPanelItemProps) => {
  const liveElapsed = useLiveElapsedTime(elapsedTime);
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex w-full cursor-pointer items-center justify-between rounded-lg px-4 py-3 ${
        isActive
          ? "bg-(--color-action-active)"
          : "bg-(--color-bg-surface) hover:bg-(--color-action-hover)"
      }`}
    >
      <span className="typo-subtitle5 text-(--color-text-primary) whitespace-nowrap">
        {title}
      </span>
      <span className="flex items-center gap-0.5">
        <Icon icon={IconClock} size={16} className="text-green-700" />
        <span className="typo-body6 text-green-700 whitespace-nowrap">
          {liveElapsed}
        </span>
      </span>
    </button>
  );
};

const MeetingPanelItem = ({
  title,
  isActive = false,
  isLive = false,
  elapsedTime,
  date,
  badgeCount,
  onClick,
}: MeetingPanelItemProps) => {
  if (isLive) {
    return (
      <LiveMeetingPanelItem
        title={title}
        isActive={isActive}
        elapsedTime={elapsedTime}
        onClick={onClick}
      />
    );
  }

  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex w-full cursor-pointer flex-col items-start gap-2 rounded-lg px-4 py-3 ${
        isActive
          ? "bg-(--color-action-active)"
          : "bg-(--color-bg-surface) hover:bg-(--color-action-hover)"
      }`}
    >
      <span className="flex w-full items-center gap-2">
        <span className="typo-subtitle5 text-(--color-text-primary) w-47 text-left">
          {title}
        </span>
        {badgeCount != null && badgeCount > 0 && (
          <span className="flex w-5 items-center justify-center rounded-full bg-green-500">
            <span className="typo-label text-(--color-text-inverse) leading-5">
              {badgeCount}
            </span>
          </span>
        )}
      </span>
      {date && (
        <span className="typo-caption1 text-(--color-text-tertiary) w-full text-left">
          {date}
        </span>
      )}
    </button>
  );
};

export default MeetingPanelItem;
