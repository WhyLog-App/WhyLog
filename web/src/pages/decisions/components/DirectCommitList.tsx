import { useRef } from "react";
import type { RepositoryCommitItem } from "@/types/git";
import CommitCard from "./CommitCard";

interface DirectCommitListProps {
  repositories: { repository_id: number; name: string }[];
  selectedRepositoryId: number | null;
  repositoryName: string;
  onSelectRepository: (repositoryId: number) => void;
  commits: RepositoryCommitItem[];
  hasNextPage: boolean;
  isFetchingNextPage: boolean;
  onLoadMore: () => void;
  onSelectCommit: (commit: RepositoryCommitItem) => void;
  onDragStart: (commitId: number) => void;
  onDragEnd: () => void;
}

const RepositoryTabs = ({
  repositories,
  selectedRepositoryId,
  onSelectRepository,
}: Pick<
  DirectCommitListProps,
  "repositories" | "selectedRepositoryId" | "onSelectRepository"
>) => {
  const scrollRef = useRef<HTMLDivElement>(null);
  const dragStartRef = useRef<{ x: number; scrollLeft: number } | null>(null);

  return (
    <section
      role="application"
      ref={scrollRef}
      onMouseDown={(event) => {
        dragStartRef.current = {
          x: event.clientX,
          scrollLeft: scrollRef.current?.scrollLeft ?? 0,
        };
      }}
      onMouseMove={(event) => {
        if (dragStartRef.current && scrollRef.current) {
          scrollRef.current.scrollLeft =
            dragStartRef.current.scrollLeft -
            (event.clientX - dragStartRef.current.x);
        }
      }}
      onMouseUp={() => {
        dragStartRef.current = null;
      }}
      onMouseLeave={() => {
        dragStartRef.current = null;
      }}
      className="flex min-h-10 gap-1 overflow-x-auto px-0 py-1"
    >
      {repositories.map((repository) => (
        <button
          key={repository.repository_id}
          type="button"
          onClick={() => onSelectRepository(repository.repository_id)}
          className={`shrink-0 rounded-full border px-2 py-1 typo-caption1 ${selectedRepositoryId === repository.repository_id ? "border-(--color-action-primary) bg-(--color-action-primary) text-(--color-text-inverse)" : "border-(--color-border-default) bg-(--color-bg-subtle) text-(--color-text-secondary)"}`}
        >
          {repository.name}
        </button>
      ))}
    </section>
  );
};

const DirectCommitList = ({
  repositories,
  selectedRepositoryId,
  repositoryName,
  onSelectRepository,
  commits,
  hasNextPage,
  isFetchingNextPage,
  onLoadMore,
  onSelectCommit,
  onDragStart,
  onDragEnd,
}: DirectCommitListProps) => (
  <>
    <RepositoryTabs
      repositories={repositories}
      selectedRepositoryId={selectedRepositoryId}
      onSelectRepository={onSelectRepository}
    />
    <div
      onScroll={(event) => {
        const element = event.currentTarget;
        const remaining =
          element.scrollHeight - element.scrollTop - element.clientHeight;
        if (hasNextPage && !isFetchingNextPage && remaining < 80) onLoadMore();
      }}
      className="flex flex-1 flex-col overflow-y-auto"
    >
      {commits.map((commit) => (
        <CommitCard
          key={commit.commit_id}
          hash={commit.hash}
          message={commit.message}
          repositoryName={repositoryName}
          variant="direct"
          authorName={commit.author_name}
          committedDate={commit.date_time}
          addedLines={commit.added_lines}
          removedLines={commit.deleted_lines}
          onClick={() => onSelectCommit(commit)}
          onDragStart={() => onDragStart(commit.commit_id)}
          onDragEnd={onDragEnd}
        />
      ))}
      {isFetchingNextPage ? (
        <p className="py-3 text-center typo-caption1 text-(--color-text-tertiary)">
          커밋을 더 불러오는 중입니다
        </p>
      ) : null}
    </div>
  </>
);

export default DirectCommitList;
