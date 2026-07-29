import { Navigate, Outlet, useParams } from "react-router-dom";
import { useTeams } from "@/components/sidebar/hooks/useTeams";
import { ROUTES } from "@/constants/routes";
import { parseRouteId } from "@/utils/parseRouteId";
import AppLayout from "../AppLayout";

const TeamLayout = () => {
  const { teamId } = useParams<{ teamId: string }>();
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

  const teamIdNum = parseRouteId(teamId);

  // 팀이 없으면 온보딩 페이지로
  if (!teams || teams.length === 0) {
    return <Navigate to={ROUTES.ONBOARDING_CREATE_TEAM} replace />;
  }

  // 잘못된 teamId면 첫 번째 팀으로 리다이렉트
  const isValidTeam = teams.some((team) => team.team_id === teamIdNum);
  if (!isValidTeam) {
    return <Navigate to={`/team/${teams[0].team_id}`} replace />;
  }

  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
};

export default TeamLayout;
