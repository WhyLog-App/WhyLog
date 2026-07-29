import { useQuery } from "@tanstack/react-query";
import { getMeetingDetail } from "@/apis/meetings";
import type { MeetingDetail } from "@/types/meeting";

export const MEETING_DETAIL_QUERY_KEY = ["meetings", "detail"] as const;

export const useMeetingDetail = (
  meetingId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<MeetingDetail>({
    queryKey: [...MEETING_DETAIL_QUERY_KEY, meetingId],
    queryFn: () => getMeetingDetail(meetingId as number),
    enabled: meetingId != null && (options?.enabled ?? true),
  });
