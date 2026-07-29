import { useQuery } from "@tanstack/react-query";
import { getRepositories } from "@/apis/git";
import type { RepositoryItem } from "@/types/git";

export const TEAM_REPOSITORIES_QUERY_KEY = ["teams", "repositories"] as const;

export const useRepositories = (
  teamId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<RepositoryItem[]>({
    queryKey: [...TEAM_REPOSITORIES_QUERY_KEY, teamId],
    queryFn: () => getRepositories(teamId as number),
    enabled: teamId != null && (options?.enabled ?? true),
  });
