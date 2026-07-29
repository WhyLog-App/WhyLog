import { useParams } from "react-router-dom";
import { useTeams } from "@/components/sidebar/hooks/useTeams";
import { parseRouteId } from "@/utils/parseRouteId";

export const useCurrentTeam = () => {
  const { teamId } = useParams<{ teamId: string }>();
  const { data: teams, isLoading, isError } = useTeams();

  const teamIdNum = parseRouteId(teamId);
  const currentTeam = teams?.find((t) => t.team_id === teamIdNum) ?? null;

  return {
    teamId: teamIdNum,
    currentTeam,
    isLoading,
    isError,
  };
};
