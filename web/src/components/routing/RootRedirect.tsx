import { Navigate } from "react-router-dom";
import { useTeams } from "@/components/sidebar/hooks/useTeams";
import { ROUTES } from "@/constants/routes";

const RootRedirect = () => {
  const { data: teams, isLoading, isError } = useTeams();

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg">로딩 중...</div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg text-red-500">
          팀 목록을 불러오지 못했습니다.
        </div>
      </div>
    );
  }

  if (!teams || teams.length === 0) {
    return <Navigate to={ROUTES.ONBOARDING_CREATE_TEAM} replace />;
  }

  return <Navigate to={`/team/${teams[0].team_id}`} replace />;
};

export default RootRedirect;
