import { useQuery } from "@tanstack/react-query";
import { getMeetingHistory } from "@/apis/meetings";
import type { MeetingHistory } from "@/types/meeting";

export const MEETING_HISTORY_QUERY_KEY = ["meetings", "history"] as const;

export const useMeetingHistory = (meetingId: number | null) =>
  useQuery<MeetingHistory>({
    queryKey: [...MEETING_HISTORY_QUERY_KEY, meetingId],
    queryFn: () => getMeetingHistory(meetingId as number),
    enabled: meetingId != null,
  });
