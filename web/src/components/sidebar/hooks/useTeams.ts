import { useQuery } from "@tanstack/react-query";
import { fetchTeams } from "@/apis/teams";

export const TEAMS_QUERY_KEY = ["teams"] as const;

export const useTeams = () => {
  return useQuery({
    queryKey: TEAMS_QUERY_KEY,
    queryFn: fetchTeams,
  });
};
