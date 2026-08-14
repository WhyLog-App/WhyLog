import { useEffect, useRef, useState } from "react";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import type {
  ApplicationConnectedCommit,
  ApplicationRecommendedCommit,
} from "@/types/application";
import type { RepositoryCommitItem } from "@/types/git";
import { useLinkCommit } from "../hooks/useLinkCommit";
import { useRepositories } from "../hooks/useRepositories";
import { useRepositoryCommitsInfinite } from "../hooks/useRepositoryCommitsInfinite";
import { useUnlinkCommit } from "../hooks/useUnlinkCommit";
import CommitCard from "./CommitCard";
import CommitDetailPanel, {
  type CommitDetailCommit,
} from "./CommitDetailPanel";

interface CodeApplicationTabProps {
  applicationId: number;
  applicationName: string;
  keywords: string[];
  recommendedCommits: ApplicationRecommendedCommit[];
  linkedCommits: ApplicationConnectedCommit[];
}

interface DirectCommitListProps {
  repositories: { repository_id: number; name: string }[];
  selectedRepositoryId: number | null;
  onSelectRepository: (repositoryId: number) => void;
  commits: RepositoryCommitItem[];
  onSelectCommit: (commit: RepositoryCommitItem) => void;
  onDragStart: (commitId: number) => void;
  onDragEnd: () => void;
}

const DirectCommitList = ({
  repositories,
  selectedRepositoryId,
  onSelectRepository,
  commits,
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
    <div className="flex flex-col">
      {commits.map((commit) => (
        <CommitCard
          key={commit.commit_id}
          hash={commit.hash}
          message={commit.message}
          repositoryName={
            repositories.find(
              (repository) => repository.repository_id === selectedRepositoryId,
            )?.name ?? ""
          }
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
    </div>
  </>
);

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
        if (dragStartRef.current && scrollRef.current)
          scrollRef.current.scrollLeft =
            dragStartRef.current.scrollLeft -
            (event.clientX - dragStartRef.current.x);
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
          className={`shrink-0 rounded-full border px-2 py-1 typo-caption1 ${selectedRepositoryId === repository.repository_id ? "border-blue-600 bg-blue-600 text-white" : "border-gray-300 bg-gray-100 text-gray-700"}`}
        >
          {repository.name}
        </button>
      ))}
    </section>
  );
};

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
  const [isScrolling, setIsScrolling] = useState(false);
  const scrollTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [draggedCommitId, setDraggedCommitId] = useState<number | null>(null);
  const [selectedRepositoryId, setSelectedRepositoryId] = useState<
    number | null
  >(null);
  const { teamId } = useCurrentTeam();
  const { data: repositories = [] } = useRepositories(teamId);
  const { commits: directCommits } = useRepositoryCommitsInfinite(
    selectedRepositoryId,
    { enabled: selectedRepositoryId != null },
  );
  const directCommitsByHash = new Map(
    directCommits.map((commit) => [commit.hash, commit]),
  );
  const {
    linkCommits,
    errorMessage: linkErrorMessage,
    isPending: isLinkPending,
  } = useLinkCommit(applicationId, {
    onSuccess: () =>
      setSelectedCommit((commit) =>
        commit ? { ...commit, isConnected: true } : null,
      ),
  });
  const { unlinkCommit, isPending: isUnlinkPending } = useUnlinkCommit(
    applicationId,
    {
      onSuccess: () =>
        setSelectedCommit((commit) =>
          commit ? { ...commit, isConnected: false } : null,
        ),
    },
  );

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
  const handleScroll = () => {
    setIsScrolling(true);
    if (scrollTimerRef.current) clearTimeout(scrollTimerRef.current);
    scrollTimerRef.current = setTimeout(() => setIsScrolling(false), 700);
  };
  return (
    <section className="flex min-h-0 flex-1 flex-col gap-4 overflow-hidden">
      <div className="flex items-center gap-4 rounded-xl border border-(--color-border-default) bg-white px-5 py-3">
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
        <div className="flex min-w-176 flex-1 overflow-hidden rounded-xl border border-(--color-border-default) bg-white">
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
              <div className="flex shrink-0 items-center rounded-lg bg-gray-100 p-1 typo-caption1">
                <button
                  type="button"
                  onClick={() => setSourceTab("recommended")}
                  className={`rounded-md px-3 py-1 ${sourceTab === "recommended" ? "bg-white text-blue-600 shadow-sm" : "text-gray-500"}`}
                >
                  추천 커밋
                </button>
                <button
                  type="button"
                  onClick={() => setSourceTab("direct")}
                  className={`rounded-md px-3 py-1 ${sourceTab === "direct" ? "bg-white text-blue-600 shadow-sm" : "text-gray-500"}`}
                >
                  직접 연결
                </button>
              </div>
            </div>
            <div
              onScroll={handleScroll}
              className={`application-scroll flex flex-1 flex-col overflow-y-auto ${isScrolling ? "is-scrolling" : ""}`}
            >
              {sourceTab === "recommended" ? (
                recommendedCommits.map((commit) =>
                  (() => {
                    const matchedCommit = directCommitsByHash.get(
                      commit.commit_hash,
                    );
                    return (
                      <CommitCard
                        key={commit.commit_id}
                        hash={commit.commit_hash}
                        message={commit.message}
                        repositoryName={commit.repository_name}
                        reason={commit.reason}
                        authorName={matchedCommit?.author_name}
                        committedDate={matchedCommit?.date_time}
                        addedLines={matchedCommit?.added_lines}
                        removedLines={matchedCommit?.deleted_lines}
                        variant="recommended"
                        selected={selectedCommit?.hash === commit.commit_hash}
                        onClick={() =>
                          showDetail({
                            repositoryName: commit.repository_name,
                            hash: commit.commit_hash,
                            message: commit.message,
                            commitId: Number(commit.commit_id),
                            isConnected: linkedCommits.some(
                              (linkedCommit) =>
                                linkedCommit.commit_hash === commit.commit_hash,
                            ),
                            reason: commit.reason,
                            repositoryId: repositories.find(
                              (repository) =>
                                repository.name === commit.repository_name,
                            )?.repository_id,
                            authorName: matchedCommit?.author_name,
                            committedDate: matchedCommit?.date_time,
                          })
                        }
                        onDragStart={() => startDragging(commit.commit_id)}
                        onDragEnd={finishDragging}
                      />
                    );
                  })(),
                )
              ) : (
                <DirectCommitList
                  repositories={repositories}
                  selectedRepositoryId={selectedRepositoryId}
                  onSelectRepository={setSelectedRepositoryId}
                  commits={directCommits}
                  onSelectCommit={(commit) =>
                    showDetail({
                      repositoryName:
                        repositories.find(
                          (repository) =>
                            repository.repository_id === selectedRepositoryId,
                        )?.name ?? "",
                      hash: commit.hash,
                      message: commit.message,
                      commitId: commit.commit_id,
                      isConnected: linkedCommits.some(
                        (linkedCommit) =>
                          linkedCommit.commit_hash === commit.hash,
                      ),
                      repositoryId: selectedRepositoryId ?? undefined,
                      authorName: commit.author_name,
                      committedDate: commit.date_time,
                    })
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
              <p className="typo-caption1 text-(--color-text-error)">
                {linkErrorMessage}
              </p>
            ) : null}
            <section
              role="application"
              onDragOver={(event) => {
                event.preventDefault();
                setIsDraggingCommit(true);
              }}
              onDrop={handleDrop}
              onScroll={handleScroll}
              className={`application-scroll relative flex flex-1 flex-col overflow-y-auto ${isScrolling ? "is-scrolling" : ""}`}
            >
              {linkedCommits.map((commit) => {
                const matchedCommit = directCommitsByHash.get(
                  commit.commit_hash,
                );
                return (
                  <CommitCard
                    key={commit.commit_id}
                    hash={commit.commit_hash}
                    message={commit.message}
                    repositoryName={commit.repository_name}
                    authorName={matchedCommit?.author_name}
                    committedDate={commit.committed_date}
                    addedLines={matchedCommit?.added_lines}
                    removedLines={matchedCommit?.deleted_lines}
                    variant="linked"
                    selected={selectedCommit?.hash === commit.commit_hash}
                    onClick={() =>
                      showDetail({
                        repositoryName: commit.repository_name,
                        hash: commit.commit_hash,
                        message: commit.message,
                        commitId: commit.commit_id,
                        isConnected: true,
                        repositoryId: repositories.find(
                          (repository) =>
                            repository.name === commit.repository_name,
                        )?.repository_id,
                        authorName: matchedCommit?.author_name,
                        committedDate: commit.committed_date,
                      })
                    }
                  />
                );
              })}
              {isDraggingCommit ? (
                <div className="absolute inset-0 flex flex-col items-center justify-center rounded-lg border border-dashed border-(--color-border-brand) bg-blue-50/80 px-4 text-center">
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
