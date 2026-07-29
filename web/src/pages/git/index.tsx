import { useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import EmptyStateCard from "@/components/common/EmptyStateCard";
import GitTokenModal from "@/components/panel/git/GitTokenModal";
import RepositoryAddModal from "@/components/panel/git/RepositoryAddModal";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import type {
  GitCommitItem,
  GitRepositoryStats,
  RepositoryCommitItem,
} from "@/types/git";
import { formatCommitDate } from "@/utils/date";
import { parseRouteId } from "@/utils/parseRouteId";
import GitList from "./components/GitList";
import { useAddRepository } from "./hooks/useAddRepository";
import { useCheckGitHubToken } from "./hooks/useCheckGitHubToken";
import { useGetRepositories } from "./hooks/useGetRepositories";
import { useGetRepositoryCommits } from "./hooks/useGetRepositoryCommits";
import { useRegisterGitHubToken } from "./hooks/useRegisterGitHubToken";

const mapCommitItem = (commit: RepositoryCommitItem): GitCommitItem => {
  const hasConnectedApplication = commit.connected_application !== null;

  return {
    id: String(commit.commit_id),
    hash: commit.hash,
    message: commit.message,
    decisionText: commit.connected_application?.name ?? "",
    decisionType: hasConnectedApplication ? "success" : "neutral",
    authorName: commit.author_name,
    authorProfileImage: commit.author_profile_image,
    dateText: formatCommitDate(commit.date_time),
    changesAdded: commit.added_lines,
    changesRemoved: commit.deleted_lines,
  };
};

function GitPage() {
  const queryClient = useQueryClient();
  const { teamId } = useCurrentTeam();
  const params = useParams<{ repositoryId?: string }>();
  const navigate = useNavigate();
  const { data: repositories = [] } = useGetRepositories(teamId);

  const routeRepositoryId = parseRouteId(params.repositoryId);

  const selectedRepository = useMemo(
    () =>
      repositories.find(
        (repository) => repository.repository_id === routeRepositoryId,
      ),
    [repositories, routeRepositoryId],
  );

  const repositoryId = selectedRepository?.repository_id ?? null;

  const {
    data: commitPages,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading,
  } = useGetRepositoryCommits(repositoryId);

  const commits = useMemo(
    () =>
      commitPages?.pages.flatMap((page) =>
        page.commit_dtolist.map(mapCommitItem),
      ) ?? [],
    [commitPages],
  );

  const stats: GitRepositoryStats = useMemo(() => {
    const firstPage = commitPages?.pages[0];
    return {
      commits: firstPage?.total_commit_count ?? 0,
      connected: firstPage?.connected_commit_count ?? 0,
      disconnected: firstPage?.unconnected_commit_count ?? 0,
    };
  }, [commitPages]);

  const [isTokenModalOpen, setIsTokenModalOpen] = useState(false);
  const [isRepoModalOpen, setIsRepoModalOpen] = useState(false);

  const { data: tokenStatus, isLoading: isCheckingToken } =
    useCheckGitHubToken();

  const {
    registerGitHubToken,
    isPending: isRegistering,
    errorMessage: registerError,
  } = useRegisterGitHubToken();

  const {
    addRepository,
    isPending: isAdding,
    errorMessage: addError,
  } = useAddRepository({
    onSuccess: () => {
      setIsRepoModalOpen(false);
      queryClient.invalidateQueries({
        queryKey: ["repositories", teamId],
      });
    },
    onTokenExpired: () => {
      setIsRepoModalOpen(false);
      setIsTokenModalOpen(true);
    },
  });

  const handleAction = () => {
    if (isCheckingToken) return;
    if (tokenStatus?.is_registered) {
      setIsRepoModalOpen(true);
    } else {
      setIsTokenModalOpen(true);
    }
  };

  if (!repositoryId) {
    return (
      <div className="flex min-h-full w-full items-center justify-center p-8">
        <EmptyStateCard page="Git" onAction={handleAction} />
        {isTokenModalOpen && (
          <GitTokenModal
            onClose={() => setIsTokenModalOpen(false)}
            onRegister={registerGitHubToken}
            onNext={() => {
              setIsTokenModalOpen(false);
              setIsRepoModalOpen(true);
            }}
            isPending={isRegistering}
            errorMessage={registerError}
          />
        )}
        {isRepoModalOpen && (
          <RepositoryAddModal
            onClose={() => setIsRepoModalOpen(false)}
            onAdd={(payload) => {
              if (!teamId) return;
              addRepository(teamId, payload);
            }}
            isPending={isAdding}
            errorMessage={addError}
          />
        )}
      </div>
    );
  }

  return (
    <div className="flex w-full flex-col py-8 lg:py-15">
      <GitList
        repositoryName={selectedRepository?.name ?? "Repository"}
        stats={stats}
        commits={commits}
        hasNextPage={hasNextPage}
        isLoading={isLoading}
        isFetchingNextPage={isFetchingNextPage}
        onCommitClick={(commit) => {
          if (!repositoryId) return;
          void navigate(
            `/team/${teamId}/git/${repositoryId}/${encodeURIComponent(commit.hash)}`,
          );
        }}
        onLoadMore={() => {
          void fetchNextPage();
        }}
      />
    </div>
  );
}

export default GitPage;
