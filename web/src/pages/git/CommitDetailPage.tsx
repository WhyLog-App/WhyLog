import { useEffect, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import GlassCard from "@/pages/decisions/components/GlassCard";
import { parseRouteId } from "@/utils/parseRouteId";
import CommitFileList from "./components/CommitFileList";
import CommitHeader from "./components/CommitHeader";
import CommitMeta from "./components/CommitMeta";
import { useGetCommitDetail } from "./hooks/useGetCommitDetail";
import { useGetRepositories } from "./hooks/useGetRepositories";
import { mapCommitDetail } from "./mapCommitDetail";

const CommitDetailPage = () => {
  const { teamId } = useCurrentTeam();
  const params = useParams<{ repositoryId?: string; commitHash?: string }>();
  const navigate = useNavigate();

  const repositoryId = parseRouteId(params.repositoryId);
  const { data: repositories = [] } = useGetRepositories(teamId);
  const selectedRepository = useMemo(
    () =>
      repositories.find(
        (repository) => repository.repository_id === repositoryId,
      ),
    [repositoryId, repositories],
  );

  useEffect(() => {
    if (repositories.length > 0 && !selectedRepository) {
      void navigate(`/team/${teamId}/git`, { replace: true });
    }
  }, [repositories, selectedRepository, navigate, teamId]);

  const { data: commitDetailData, isLoading: isCommitDetailLoading } =
    useGetCommitDetail(
      selectedRepository?.repository_id ?? null,
      params.commitHash,
    );

  const detail = useMemo(
    () =>
      mapCommitDetail(
        selectedRepository?.name ?? "Repository",
        commitDetailData,
        params.commitHash ?? "",
      ),
    [commitDetailData, params.commitHash, selectedRepository?.name],
  );

  const goBack = () => {
    if (selectedRepository?.repository_id) {
      void navigate(`/team/${teamId}/git/${selectedRepository.repository_id}`);
      return;
    }
    void navigate(`/team/${teamId}/git`);
  };

  if (isCommitDetailLoading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="flex w-full flex-col py-8 lg:py-[60px]">
      <div className="mx-auto flex w-full max-w-[1160px] flex-col gap-5">
        <div className="flex flex-col gap-5">
          <CommitHeader detail={detail} onBack={goBack} />
          <CommitMeta detail={detail} />
        </div>

        <GlassCard className="gap-5 px-5 py-7">
          <p className="typo-caption1 text-(--color-text-secondary)">
            변경된 파일
          </p>

          <CommitFileList files={detail.files} />
        </GlassCard>
      </div>
    </div>
  );
};

export default CommitDetailPage;
