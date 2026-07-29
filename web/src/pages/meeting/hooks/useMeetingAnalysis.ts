import { useQuery } from "@tanstack/react-query";
import { getMeetingAnalysis } from "@/apis/meetings";
import type { MeetingAnalysis } from "@/types/meeting";

export const MEETING_ANALYSIS_QUERY_KEY = ["meetings", "analysis"] as const;

export const useMeetingAnalysis = (meetingId: number | null) =>
  useQuery<MeetingAnalysis>({
    queryKey: [...MEETING_ANALYSIS_QUERY_KEY, meetingId],
    queryFn: () => getMeetingAnalysis(meetingId as number),
    enabled: meetingId != null,
    refetchInterval: (query) =>
      query.state.data && !query.state.data.is_analyzed ? 5000 : false,
  });
