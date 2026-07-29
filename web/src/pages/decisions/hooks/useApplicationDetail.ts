import { useQuery } from "@tanstack/react-query";
import { getApplicationDetail } from "@/apis/applications";
import type { ApplicationDetail } from "@/types/application";

export const APPLICATION_DETAIL_QUERY_KEY = ["applications", "detail"] as const;

export const useApplicationDetail = (
  applicationId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<ApplicationDetail>({
    queryKey: [...APPLICATION_DETAIL_QUERY_KEY, applicationId],
    queryFn: () => getApplicationDetail(applicationId as number),
    enabled: applicationId != null && (options?.enabled ?? true),
  });
