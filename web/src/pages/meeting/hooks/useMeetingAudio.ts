import { useQuery } from "@tanstack/react-query";
import { getMeetingAudio } from "@/apis/meetings";
import type { MeetingAudio } from "@/types/meeting";

export const MEETING_AUDIO_QUERY_KEY = ["meetings", "audio"] as const;

export const useMeetingAudio = (meetingId: number | null) =>
  useQuery<MeetingAudio>({
    queryKey: [...MEETING_AUDIO_QUERY_KEY, meetingId],
    queryFn: () => getMeetingAudio(meetingId as number),
    enabled: meetingId != null,
    staleTime: 9 * 60 * 1000,
    retry: false,
  });
