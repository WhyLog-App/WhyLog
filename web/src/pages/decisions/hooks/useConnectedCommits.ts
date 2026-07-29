import { useQuery } from "@tanstack/react-query";
import { getConnectedCommits } from "@/apis/applications";
import type { ApplicationConnectedCommitsResult } from "@/types/application";

export const APPLICATION_CONNECTED_COMMITS_QUERY_KEY = [
  "applications",
  "connected-commits",
] as const;

export const useConnectedCommits = (
  applicationId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<ApplicationConnectedCommitsResult>({
    queryKey: [...APPLICATION_CONNECTED_COMMITS_QUERY_KEY, applicationId],
    queryFn: () => getConnectedCommits(applicationId as number),
    enabled: applicationId != null && (options?.enabled ?? true),
  });
