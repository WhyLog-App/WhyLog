import IconClock from "@/assets/icons/media/ic_clock.svg?react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import type { GitCommitDetailItem } from "@/types/git";

interface Props {
  detail: GitCommitDetailItem;
}

const CommitMeta = ({ detail }: Props) => {
  return (
    <div className="flex w-full flex-wrap items-center justify-between gap-x-4 gap-y-2 rounded-[14px] border border-white bg-(--color-white-50) px-6 py-3">
      <div className="flex min-w-0 items-center gap-1">
        <div className="w-[30px] h-[30px] rounded-full overflow-hidden bg-(--color-purple-50) flex items-center justify-center flex-shrink-0">
          {detail.authorProfileImage ? (
            <img
              src={detail.authorProfileImage}
              alt="avatar"
              className="w-full h-full object-cover"
            />
          ) : (
            <Icon icon={IconCircleUser} size={14} className="text-current" />
          )}
        </div>
        <div className="flex min-w-0 flex-col items-start">
          <div className="flex min-w-0 items-center gap-1">
            <p className="typo-subtitle5 shrink-0 text-(--color-text-primary)">
              {detail.authorName}
            </p>
            <p className="typo-caption1 truncate text-(--color-text-secondary)">
              {detail.authorEmail}
            </p>
          </div>
          <div className="flex items-center gap-1 text-(--color-text-tertiary)">
            <Icon icon={IconClock} size={14} className="text-current" />
            <span className="typo-caption2">{detail.dateText}</span>
          </div>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <div className="inline-flex items-center rounded-[4px] bg-[#e8f5e9] px-2 typo-caption1 font-semibold text-[#2e7d32] h-[22px]">
          +{detail.changesAdded}
        </div>
        <div className="inline-flex items-center rounded-[4px] bg-(--color-red-50) px-2 typo-caption1 font-semibold text-(--color-red-700) h-[22px]">
          -{detail.changesRemoved}
        </div>
        <div className="inline-flex items-center rounded-[4px] bg-(--color-black-10) px-2 typo-caption1 font-semibold text-(--color-gray-800) h-[22px]">
          {detail.files.length} files
        </div>
      </div>
    </div>
  );
};

export default CommitMeta;
