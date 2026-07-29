import { useQuery } from "@tanstack/react-query";
import { listDecisions } from "@/apis/teams";

export const DECISIONS_QUERY_KEY = (teamId: number) =>
  ["team", teamId, "decisions"] as const;

export const useDecisions = (teamId: number | null) => {
  return useQuery({
    queryKey: DECISIONS_QUERY_KEY(teamId ?? 0),
    queryFn: () => {
      if (typeof teamId !== "number" || teamId <= 0) return [];
      return listDecisions(teamId);
    },
    enabled: typeof teamId === "number" && teamId > 0,
  });
};
