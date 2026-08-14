import { useEffect, useMemo, useState } from "react";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import type {
  ApplicationConnectedCommit,
  ApplicationRecommendedCommit,
} from "@/types/application";
import { useLinkCommit } from "../hooks/useLinkCommit";
import { useRepositories } from "../hooks/useRepositories";
import { useRepositoryCommitsInfinite } from "../hooks/useRepositoryCommitsInfinite";
import { useUnlinkCommit } from "../hooks/useUnlinkCommit";
import CommitCard from "./CommitCard";
import CommitDetailPanel, {
  type CommitDetailCommit,
} from "./CommitDetailPanel";
import DirectCommitList from "./DirectCommitList";

interface CodeApplicationTabProps {
  applicationId: number;
  applicationName: string;
  keywords: string[];
  recommendedCommits: ApplicationRecommendedCommit[];
  linkedCommits: ApplicationConnectedCommit[];
}

interface DetailCommitInput {
  repositoryName: string;
  repositoryId?: number;
  hash: string;
  message: string;
  commitId?: number;
  isConnected: boolean;
  reason?: string;
  authorName?: string;
  committedDate?: string;
}

const toDetailCommit = ({
  repositoryName,
  repositoryId,
  hash,
  message,
  commitId,
  isConnected,
  reason,
  authorName,
  committedDate,
}: DetailCommitInput): CommitDetailCommit => ({
  repositoryName,
  repositoryId,
  hash,
  message,
  commitId,
  isConnected,
  reason,
  authorName,
  committedDate,
});

const CodeApplicationTab = ({
  applicationId,
  applicationName,
  keywords,
  recommendedCommits,
  linkedCommits,
}: CodeApplicationTabProps) => {
  const [sourceTab, setSourceTab] = useState<"recommended" | "direct">(
    "recommended",
  );
  const [selectedCommit, setSelectedCommit] =
    useState<CommitDetailCommit | null>(null);
  const [detailCollapsed, setDetailCollapsed] = useState(true);
  const [isDraggingCommit, setIsDraggingCommit] = useState(false);
  const [draggedCommitId, setDraggedCommitId] = useState<number | null>(null);
  const [selectedRepositoryId, setSelectedRepositoryId] = useState<
    number | null
  >(null);
  const { teamId } = useCurrentTeam();
  const { data: repositories = [] } = useRepositories(teamId);
  const {
    commits: directCommits,
    hasNextPage: hasNextDirectPage,
    isFetchingNextPage: isFetchingNextDirectPage,
    fetchNextPage: fetchNextDirectPage,
  } = useRepositoryCommitsInfinite(selectedRepositoryId, {
    enabled: selectedRepositoryId != null,
  });
  const repositoryNameById = useMemo(
    () =>
      new Map(
        repositories.map((repository) => [
          repository.repository_id,
          repository.name,
        ]),
      ),
    [repositories],
  );
  const repositoryIdByName = useMemo(
    () =>
      new Map(
        repositories.map((repository) => [
          repository.name,
          repository.repository_id,
        ]),
      ),
    [repositories],
  );
  const linkedCommitHashes = useMemo(
    () => new Set(linkedCommits.map((commit) => commit.commit_hash)),
    [linkedCommits],
  );
  const {
    linkCommits,
    errorMessage: linkErrorMessage,
    isPending: isLinkPending,
  } = useLinkCommit(applicationId, {
    onSuccess: (commitIds) =>
      setSelectedCommit((commit) =>
        commit && commit.commitId != null && commitIds.includes(commit.commitId)
          ? { ...commit, isConnected: true }
          : commit,
      ),
  });
  const {
    unlinkCommit,
    errorMessage: unlinkErrorMessage,
    isPending: isUnlinkPending,
  } = useUnlinkCommit(applicationId, {
    onSuccess: (commitId) =>
      setSelectedCommit((commit) =>
        commit?.commitId === commitId
          ? { ...commit, isConnected: false }
          : commit,
      ),
  });

  useEffect(() => {
    if (selectedRepositoryId == null && repositories[0]) {
      setSelectedRepositoryId(repositories[0].repository_id);
    }
  }, [repositories, selectedRepositoryId]);

  const showDetail = (commit: CommitDetailCommit) => {
    setSelectedCommit(commit);
    setDetailCollapsed(false);
  };
  const startDragging = (commitId: string | number) => {
    const numericId = Number(commitId);
    setDraggedCommitId(Number.isInteger(numericId) ? numericId : null);
    setIsDraggingCommit(true);
  };
  const finishDragging = () => {
    setDraggedCommitId(null);
    setIsDraggingCommit(false);
  };
  const handleDrop = () => {
    if (draggedCommitId != null) linkCommits([draggedCommitId]);
    finishDragging();
  };
  return (
    <section className="flex min-h-0 flex-1 flex-col gap-4 overflow-hidden">
      <div className="flex items-center gap-4 rounded-xl border border-(--color-border-default) bg-(--color-bg-surface) px-5 py-3">
        <span className="typo-body6 text-(--color-text-secondary)">
          <i className="mr-2 inline-block size-2 rounded-full bg-(--color-text-brand)" />
          추천{" "}
          <strong className="ml-1 typo-subtitle4 text-(--color-text-primary)">
            {recommendedCommits.length}
          </strong>
        </span>
        <span className="h-5 w-px bg-(--color-border-default)" />
        <span className="typo-body6 text-(--color-text-secondary)">
          <i className="mr-2 inline-block size-2 rounded-full bg-(--color-green-500)" />
          연결됨{" "}
          <strong className="ml-1 typo-subtitle4 text-(--color-text-primary)">
            {linkedCommits.length}
          </strong>
        </span>
        <span className="ml-auto typo-body6 text-(--color-text-secondary)">
          전체 {recommendedCommits.length + linkedCommits.length}
        </span>
      </div>
      <div className="flex min-h-0 flex-1 gap-4 overflow-x-auto">
        <div className="flex min-w-176 flex-1 overflow-hidden rounded-xl border border-(--color-border-default) bg-(--color-bg-surface)">
          <section className="flex min-w-0 flex-1 flex-col border-r border-(--color-border-default) p-5">
            <div
              className={`flex items-center justify-between gap-2 ${sourceTab === "direct" ? "mb-1" : "mb-3"}`}
            >
              <div className="flex items-center gap-1">
                <i className="size-1.5 rounded-full bg-(--color-text-brand)" />
                <h2 className="typo-subtitle4 text-(--color-text-primary)">
                  커밋 가져오기
                </h2>
              </div>
              <div className="flex shrink-0 items-center rounded-lg bg-(--color-bg-subtle) p-1 typo-caption1">
                <button
                  type="button"
                  onClick={() => setSourceTab("recommended")}
                  className={`rounded-md px-3 py-1 ${sourceTab === "recommended" ? "bg-(--color-bg-surface) text-(--color-text-brand) shadow-sm" : "text-(--color-text-secondary)"}`}
                >
                  추천 커밋
                </button>
                <button
                  type="button"
                  onClick={() => setSourceTab("direct")}
                  className={`rounded-md px-3 py-1 ${sourceTab === "direct" ? "bg-(--color-bg-surface) text-(--color-text-brand) shadow-sm" : "text-(--color-text-secondary)"}`}
                >
                  직접 연결
                </button>
              </div>
            </div>
            <div className="application-scroll flex flex-1 flex-col overflow-y-auto">
              {sourceTab === "recommended" ? (
                recommendedCommits.map((commit) => {
                  return (
                    <CommitCard
                      key={commit.commit_id}
                      hash={commit.commit_hash}
                      message={commit.message}
                      repositoryName={commit.repository_name}
                      reason={commit.reason}
                      authorName={commit.author_name}
                      addedLines={commit.added_lines}
                      removedLines={commit.deleted_lines}
                      variant="recommended"
                      selected={selectedCommit?.hash === commit.commit_hash}
                      onClick={() =>
                        showDetail(
                          toDetailCommit({
                            repositoryName: commit.repository_name,
                            repositoryId: repositoryIdByName.get(
                              commit.repository_name,
                            ),
                            hash: commit.commit_hash,
                            message: commit.message,
                            commitId: Number(commit.commit_id),
                            isConnected: linkedCommitHashes.has(
                              commit.commit_hash,
                            ),
                            reason: commit.reason,
                            authorName: commit.author_name,
                          }),
                        )
                      }
                      onDragStart={() => startDragging(commit.commit_id)}
                      onDragEnd={finishDragging}
                    />
                  );
                })
              ) : (
                <DirectCommitList
                  repositories={repositories}
                  selectedRepositoryId={selectedRepositoryId}
                  repositoryName={
                    selectedRepositoryId == null
                      ? ""
                      : (repositoryNameById.get(selectedRepositoryId) ?? "")
                  }
                  onSelectRepository={setSelectedRepositoryId}
                  commits={directCommits}
                  hasNextPage={hasNextDirectPage}
                  isFetchingNextPage={isFetchingNextDirectPage}
                  onLoadMore={() => {
                    void fetchNextDirectPage();
                  }}
                  onSelectCommit={(commit) =>
                    showDetail(
                      toDetailCommit({
                        repositoryName:
                          selectedRepositoryId == null
                            ? ""
                            : (repositoryNameById.get(selectedRepositoryId) ??
                              ""),
                        repositoryId: selectedRepositoryId ?? undefined,
                        hash: commit.hash,
                        message: commit.message,
                        commitId: commit.commit_id,
                        isConnected: linkedCommitHashes.has(commit.hash),
                        authorName: commit.author_name,
                        committedDate: commit.date_time,
                      }),
                    )
                  }
                  onDragStart={startDragging}
                  onDragEnd={finishDragging}
                />
              )}
            </div>
          </section>
          <section className="flex min-w-0 flex-1 flex-col gap-3 p-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-1">
                <i className="size-1.5 rounded-full bg-(--color-green-500)" />
                <h2 className="typo-subtitle4 text-(--color-text-primary)">
                  연결된 커밋 ({linkedCommits.length})
                </h2>
              </div>
            </div>
            {linkErrorMessage ? (
              <p className="typo-caption1 text-(--color-status-error)">
                {linkErrorMessage}
              </p>
            ) : null}
            {unlinkErrorMessage ? (
              <p className="typo-caption1 text-(--color-status-error)">
                {unlinkErrorMessage}
              </p>
            ) : null}
            <section
              role="application"
              onDragOver={(event) => {
                event.preventDefault();
                setIsDraggingCommit(true);
              }}
              onDrop={handleDrop}
              className="application-scroll relative flex flex-1 flex-col overflow-y-auto"
            >
              {linkedCommits.map((commit) => {
                return (
                  <CommitCard
                    key={commit.commit_id}
                    hash={commit.commit_hash}
                    message={commit.message}
                    repositoryName={commit.repository_name}
                    authorName={commit.author_name}
                    committedDate={commit.committed_date}
                    addedLines={commit.added_lines}
                    removedLines={commit.deleted_lines}
                    variant="linked"
                    selected={selectedCommit?.hash === commit.commit_hash}
                    onClick={() =>
                      showDetail(
                        toDetailCommit({
                          repositoryName: commit.repository_name,
                          hash: commit.commit_hash,
                          message: commit.message,
                          commitId: commit.commit_id,
                          isConnected: true,
                          repositoryId: repositoryIdByName.get(
                            commit.repository_name,
                          ),
                          authorName: commit.author_name,
                          committedDate: commit.committed_date,
                        }),
                      )
                    }
                  />
                );
              })}
              {isDraggingCommit ? (
                <div className="absolute inset-0 flex flex-col items-center justify-center rounded-lg border border-dashed border-(--color-border-brand) bg-(--color-bg-brand-subtle)/80 px-4 text-center">
                  <p className="typo-button-sm text-(--color-text-brand)">
                    여기에 드래그하여 연결
                  </p>
                  <p className="mt-1 typo-caption1 text-(--color-text-secondary)">
                    커밋을 이 영역에 드롭하면 연결됩니다
                  </p>
                </div>
              ) : null}
            </section>
          </section>
        </div>
        <CommitDetailPanel
          applicationName={applicationName}
          keywords={keywords}
          commit={selectedCommit}
          collapsed={detailCollapsed}
          onToggle={() => setDetailCollapsed((value) => !value)}
          onConnect={(commitId) => linkCommits([commitId])}
          onUnlink={unlinkCommit}
          isPending={isLinkPending || isUnlinkPending}
        />
      </div>
    </section>
  );
};

export default CodeApplicationTab;
