import { useEffect, useId, useMemo, useState } from "react";
import { createPortal } from "react-dom";
import IconArrowRight from "@/assets/icons/arrow/ic_arrow_right_md.svg?react";
import IconSearch from "@/assets/icons/interface/ic_search.svg?react";
import IconCloseMd from "@/assets/icons/menu/ic_close_md.svg?react";
import { Icon } from "@/components/common/Icon";
import type { RepositoryCommitItem, RepositoryItem } from "@/types/git";
import { formatCommittedDate } from "@/utils/date";
import { useLinkCommit } from "../../hooks/useLinkCommit";
import { useRepositories } from "../../hooks/useRepositories";
import { useRepositoryCommitsInfinite } from "../../hooks/useRepositoryCommitsInfinite";
import CommitHashBadge from "../CommitHashBadge";
import CommitSelectPanel from "./CommitSelectPanel";
import RepositorySidebar from "./RepositorySidebar";

interface CommitMatchModalProps {
  applicationId: number;
  teamId: number;
  onClose: () => void;
}

const CommitMatchModal = ({
  applicationId,
  teamId,
  onClose,
}: CommitMatchModalProps) => {
  const titleId = useId();
  const [selectedRepoId, setSelectedRepoId] = useState<number | null>(null);
  const [selectedCommitIds, setSelectedCommitIds] = useState<Set<number>>(
    () => new Set(),
  );
  const [keyword, setKeyword] = useState("");

  const {
    data: repositories,
    isLoading: isRepoLoading,
    isError: isRepoError,
  } = useRepositories(teamId);

  // Auto-select the first repo when list arrives
  useEffect(() => {
    if (
      selectedRepoId == null &&
      repositories &&
      repositories.length > 0 &&
      repositories[0].repository_id != null
    ) {
      setSelectedRepoId(repositories[0].repository_id);
    }
  }, [repositories, selectedRepoId]);

  // Reset selection when switching repo
  useEffect(() => {
    setSelectedCommitIds(new Set());
    setKeyword("");
  }, []);

  const handleSelectRepo = (repoId: number) => {
    if (repoId === selectedRepoId) return;
    setSelectedRepoId(repoId);
    setSelectedCommitIds(new Set());
    setKeyword("");
  };

  const {
    commits,
    isLoading: isCommitsLoading,
    isError: isCommitsError,
    hasNextPage,
    isFetchingNextPage,
    fetchNextPage,
  } = useRepositoryCommitsInfinite(selectedRepoId);

  const normalizedKeyword = keyword.trim().toLowerCase();
  const filteredCommits = useMemo<RepositoryCommitItem[]>(() => {
    if (!normalizedKeyword) return commits;
    return commits.filter(
      (c) =>
        c.message.toLowerCase().includes(normalizedKeyword) ||
        c.hash.toLowerCase().includes(normalizedKeyword),
    );
  }, [commits, normalizedKeyword]);

  const selectedRepo: RepositoryItem | null = useMemo(() => {
    if (selectedRepoId == null || !repositories) return null;
    return repositories.find((r) => r.repository_id === selectedRepoId) ?? null;
  }, [repositories, selectedRepoId]);

  const handleToggleCommit = (commitId: number) => {
    setSelectedCommitIds((prev) => {
      const next = new Set(prev);
      if (next.has(commitId)) {
        next.delete(commitId);
      } else {
        next.add(commitId);
      }
      return next;
    });
  };

  const { linkCommits, isPending, errorMessage } = useLinkCommit(
    applicationId,
    { onSuccess: onClose },
  );

  const handleSubmit = () => {
    if (selectedCommitIds.size === 0 || isPending) return;
    linkCommits(Array.from(selectedCommitIds));
  };

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  const selectedCount = selectedCommitIds.size;
  const isPrimaryDisabled = selectedCount === 0 || isPending;

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
    >
      <button
        type="button"
        className="absolute inset-0 bg-(--color-overlay-dim)"
        onClick={onClose}
        aria-label="모달 닫기"
      />

      <div className="relative flex h-[600px] w-[880px] max-w-[92vw] flex-col overflow-hidden rounded-3xl border border-(--color-border-default) bg-(--color-bg-subtle) shadow-[0px_2px_8px_0px_rgba(40,41,61,0.08),0px_20px_32px_0px_rgba(96,97,112,0.24)]">
        {/* Header */}
        <div className="flex items-center justify-between px-7 pt-6 pb-4">
          <h3 id={titleId} className="typo-h5 text-(--color-text-primary)">
            커밋 직접 매칭
          </h3>
          <button
            type="button"
            className="flex cursor-pointer items-center justify-center rounded-lg py-1"
            onClick={onClose}
            aria-label="닫기"
          >
            <Icon
              icon={IconCloseMd}
              size={24}
              className="text-(--color-text-primary)"
            />
          </button>
        </div>

        {/* Body */}
        <div className="flex min-h-0 flex-1 border-t border-(--color-border-default)">
          <RepositorySidebar
            repositories={repositories ?? []}
            selectedRepoId={selectedRepoId}
            isLoading={isRepoLoading}
            isError={isRepoError}
            onSelect={handleSelectRepo}
            IconArrowRight={IconArrowRight}
          />

          <CommitSelectPanel
            repoName={selectedRepo?.name ?? ""}
            commits={filteredCommits}
            isLoading={isCommitsLoading}
            isError={isCommitsError}
            hasNextPage={hasNextPage}
            isFetchingNextPage={isFetchingNextPage}
            fetchNextPage={() => {
              void fetchNextPage();
            }}
            keyword={keyword}
            onKeywordChange={setKeyword}
            selectedCommitIds={selectedCommitIds}
            onToggleCommit={handleToggleCommit}
            renderHash={(hash) => <CommitHashBadge hash={hash} />}
            formatDate={formatCommittedDate}
            IconSearch={IconSearch}
          />
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between border-t border-(--color-border-default) px-7 py-4">
          <p className="typo-body6 text-(--color-text-secondary)">
            <span className="typo-button-sm text-(--color-text-brand)">
              {selectedCount}
            </span>
            개의 커밋 선택됨
          </p>

          <div className="flex items-center gap-3">
            {errorMessage ? (
              <p className="typo-caption1 text-(--color-text-error)">
                {errorMessage}
              </p>
            ) : null}
            <button
              type="button"
              className="typo-button-md flex cursor-pointer items-center justify-center rounded-xl border-[1.5px] border-(--color-border-default) bg-white/50 px-5 py-2.5 text-(--color-text-secondary)"
              onClick={onClose}
            >
              취소
            </button>
            <button
              type="button"
              onClick={handleSubmit}
              disabled={isPrimaryDisabled}
              className="typo-button-md flex cursor-pointer items-center justify-center rounded-xl px-5 py-2.5 text-(--color-text-inverse) shadow-[0px_4px_12px_0px_rgba(30,91,232,0.2)] disabled:cursor-not-allowed disabled:opacity-50"
              style={{
                backgroundImage:
                  "linear-gradient(147deg, #5b8def 15.47%, #0063f7 84.42%)",
              }}
            >
              {isPending ? "연결 중..." : "선택한 커밋 매칭하기"}
            </button>
          </div>
        </div>
      </div>
    </div>,
    document.body,
  );
};

export default CommitMatchModal;
