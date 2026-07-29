import IconChevronDownDuo from "@/assets/icons/arrow/ic_chevron_down_duo.svg?react";
import { Icon } from "@/components/common/Icon";
import type { GitCommitItem, GitRepositoryStats } from "@/types/git";
import CommitTable from "./CommitTable";
import GitHeader from "./GitHeader";

interface GitListProps {
  repositoryName: string;
  stats: GitRepositoryStats;
  commits: GitCommitItem[];
  hasNextPage?: boolean;
  isLoading?: boolean;
  isFetchingNextPage?: boolean;
  onLoadMore?: () => void;
  onCommitClick?: (commit: GitCommitItem) => void;
}

const GitList = ({
  repositoryName,
  stats,
  commits,
  hasNextPage = false,
  isLoading = false,
  isFetchingNextPage = false,
  onLoadMore,
  onCommitClick,
}: GitListProps) => {
  if (isLoading) {
    return (
      <div className="mx-auto flex w-full max-w-[1160px] items-center justify-center rounded-[16px] border border-white bg-[rgba(255,255,255,0.5)] py-20 text-(--color-text-secondary)">
        커밋 목록을 불러오는 중입니다.
      </div>
    );
  }

  if (commits.length === 0) {
    return (
      <div className="mx-auto flex w-full max-w-[1160px] flex-col gap-5">
        <GitHeader repositoryName={repositoryName} stats={stats} />
        <div className="flex min-h-[240px] items-center justify-center rounded-[16px] border border-white bg-[rgba(255,255,255,0.5)] px-5 py-7 text-(--color-text-secondary)">
          커밋 내역이 없습니다.
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto flex w-full max-w-[1160px] flex-col gap-5">
      <GitHeader repositoryName={repositoryName} stats={stats} />

      <div className="overflow-hidden rounded-[16px] border border-white bg-[rgba(255,255,255,0.5)] px-5 pt-7 pb-7">
        <div className="overflow-x-auto">
          <CommitTable commits={commits} onRowClick={onCommitClick} />
        </div>
      </div>

      {hasNextPage && onLoadMore && (
        <div className="flex justify-center">
          <button
            type="button"
            onClick={onLoadMore}
            disabled={isFetchingNextPage}
            className="flex items-center gap-2 rounded-full px-4 py-1.5 text-(--color-text-secondary) transition-colors hover:text-(--color-text-primary) disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Icon
              icon={IconChevronDownDuo}
              size={16}
              className="text-current"
            />
            <span className="typo-body5 font-medium">
              {isFetchingNextPage ? "불러오는 중..." : "더 불러오기"}
            </span>
          </button>
        </div>
      )}
    </div>
  );
};

export default GitList;
