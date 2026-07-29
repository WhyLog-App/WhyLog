import IconChevronLeft from "@/assets/icons/arrow/ic_chevron_left.svg?react";
import IconGit from "@/assets/icons/media/ic_git.svg?react";
import { Icon } from "@/components/common/Icon";
import type { GitCommitDetailItem } from "@/types/git";

interface Props {
  detail: GitCommitDetailItem;
  onBack?: () => void;
}

const CommitHeader = ({ detail, onBack }: Props) => {
  return (
    <div className="flex w-full flex-col">
      {onBack && (
        <button
          type="button"
          onClick={onBack}
          className="self-start mb-2 flex items-center gap-1 typo-caption1 text-(--color-text-secondary) hover:text-(--color-text-primary) transition-colors"
        >
          <Icon icon={IconChevronLeft} size={14} className="text-current" />
          커밋 목록
        </button>
      )}
      <div className="flex items-center gap-1 mt-5">
        <Icon
          icon={IconGit}
          size={12}
          className="shrink-0 text-(--color-text-brand)"
        />
        <p className="typo-caption1 text-(--color-text-brand) truncate">
          {detail.repositoryName}
        </p>
        <p className="typo-caption1 text-(--color-text-secondary)">/</p>
        <p className="typo-caption1 text-(--color-text-secondary)">commits</p>
      </div>

      <div className="flex flex-col">
        <div className="flex items-center justify-between gap-4">
          <p className="typo-h4 text-(--color-text-primary) min-w-0 mb-3 truncate">
            {detail.message}
          </p>
          <span className="shrink-0 inline-flex rounded-[4px] bg-(--color-purple-50) px-3 h-[22px] items-center font-mono text-caption2 text-(--color-purple-700)">
            {detail.hash.slice(0, 7)}
          </span>
        </div>
        <p className="typo-body5 text-(--color-text-secondary) max-w-3xl">
          {detail.description}
        </p>
      </div>
    </div>
  );
};

export default CommitHeader;
