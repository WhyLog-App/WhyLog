import IconEditPen from "@/assets/icons/edit/ic_edit_pen.svg?react";
import IconTrash from "@/assets/icons/edit/ic_trash.svg?react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";

interface CompletedMeetingHeaderProps {
  name: string;
  startText: string;
  durationText: string;
  memberCount: number;
  members: { name: string; profile_image: string | null }[];
  onDeleteClick?: () => void;
  isDeleting?: boolean;
}

const CompletedMeetingHeader = ({
  name,
  startText,
  durationText,
  memberCount,
  members,
  onDeleteClick,
  isDeleting = false,
}: CompletedMeetingHeaderProps) => {
  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-start justify-between">
        <div className="flex flex-col gap-1">
          <h1 className="typo-h5 text-(--color-text-primary)">{name}</h1>
          <span className="typo-caption text-(--color-text-tertiary)">
            {startText} {durationText} {memberCount}명
          </span>
        </div>
        <div className="flex items-center gap-3">
          <button
            type="button"
            className="cursor-pointer text-(--color-text-tertiary) hover:text-(--color-text-secondary)"
            aria-label="회의 정보 수정"
          >
            <Icon icon={IconEditPen} size={16} />
          </button>
          {onDeleteClick && (
            <button
              type="button"
              className="cursor-pointer text-(--color-text-tertiary) hover:text-red-500 disabled:cursor-not-allowed disabled:opacity-50"
              onClick={onDeleteClick}
              disabled={isDeleting}
              aria-label="회의 삭제"
            >
              <Icon icon={IconTrash} size={16} />
            </button>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <span className="typo-subtitle5 text-(--color-text-secondary)">
          대화 기록
        </span>
        <div className="flex flex-wrap items-center gap-2">
          {members.map((m, idx) => (
            <span
              // biome-ignore lint/suspicious/noArrayIndexKey: mock list of members
              key={idx}
              className="flex items-center gap-1 rounded-full bg-(--color-bg-subtle) px-2 py-1"
            >
              {m.profile_image ? (
                <img
                  src={m.profile_image}
                  alt={m.name}
                  className="size-5 shrink-0 rounded-full object-cover"
                />
              ) : (
                <Icon
                  icon={IconCircleUser}
                  size={20}
                  className="text-(--color-dark-100)"
                />
              )}
              <span className="typo-caption text-(--color-text-secondary)">
                {m.name}
              </span>
            </span>
          ))}
        </div>
      </div>
    </div>
  );
};

export default CompletedMeetingHeader;
