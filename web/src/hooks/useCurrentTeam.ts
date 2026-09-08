import { useParams } from "react-router-dom";
import { useTeams } from "@/components/sidebar/hooks/useTeams";
import { parseRouteId } from "@/utils/parseRouteId";

export const useCurrentTeam = () => {
  const { teamId } = useParams<{ teamId: string }>();
  const { data: teams, isLoading, isError } = useTeams();

  const teamIdNum = parseRouteId(teamId);
  const currentTeam = teamId
    ? (teams?.find((team) => team.team_id === teamIdNum) ?? null)
    : (teams?.[0] ?? null);

  return {
    teamId: currentTeam?.team_id ?? teamIdNum,
    currentTeam,
    isLoading,
    isError,
  };
};
