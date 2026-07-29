import { useQuery } from "@tanstack/react-query";
import { getRecommendedCommits } from "@/apis/applications";
import type { ApplicationRecommendedCommit } from "@/types/application";

export const APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY = [
  "applications",
  "recommended-commits",
] as const;

export const useRecommendedCommits = (
  applicationId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<ApplicationRecommendedCommit[]>({
    queryKey: [...APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY, applicationId],
    queryFn: () => getRecommendedCommits(applicationId as number),
    enabled: applicationId != null && (options?.enabled ?? true),
  });
