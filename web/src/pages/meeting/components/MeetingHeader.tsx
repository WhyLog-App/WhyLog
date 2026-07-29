import IconClock from "@/assets/icons/media/ic_clock.svg?react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import type { RoomParticipant } from "../types";

interface MeetingHeaderProps {
  meetingName: string;
  meetingStart: string | null;
  memberCount: number;
  elapsed: string;
  isWsConnected: boolean;
  isRoomConnected: boolean;
  participants: RoomParticipant[];
}

const MeetingHeader = ({
  meetingName,
  meetingStart,
  memberCount,
  elapsed,
  isWsConnected,
  isRoomConnected,
  participants,
}: MeetingHeaderProps) => {
  return (
    <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-2">
      <div className="flex flex-col gap-1">
        <h1 className="typo-h5 text-(--color-text-primary)">{meetingName}</h1>
        {meetingStart && (
          <span className="typo-caption text-(--color-text-tertiary)">
            {meetingStart}
            {memberCount > 0 ? ` · 참여자 ${memberCount}명` : ""}
          </span>
        )}
      </div>
      <div className="flex items-center gap-3">
        <span className="flex items-center gap-1 rounded-full bg-green-50 px-3 py-1">
          <Icon icon={IconClock} size={16} className="text-green-700" />
          <span className="typo-body6 text-green-700">{elapsed}</span>
        </span>
        <span className="typo-caption text-(--color-text-tertiary)">
          WS {isWsConnected ? "✓" : "…"} · RTC {isRoomConnected ? "✓" : "…"}
        </span>
        <div className="flex -space-x-2">
          {participants.slice(0, 3).map((p) =>
            p.profileImage ? (
              <img
                key={p.id}
                src={p.profileImage}
                alt={p.name}
                className="h-7 w-7 rounded-full border-2 border-(--color-bg-surface) object-cover"
              />
            ) : (
              <span
                key={p.id}
                className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-(--color-bg-surface) bg-light-500"
              >
                <Icon
                  icon={IconCircleUser}
                  size={20}
                  className="text-(--color-dark-100)"
                />
              </span>
            ),
          )}
        </div>
      </div>
    </div>
  );
};

export default MeetingHeader;
