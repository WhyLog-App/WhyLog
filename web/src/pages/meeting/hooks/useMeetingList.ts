import { useQuery } from "@tanstack/react-query";
import { listMeetings } from "@/apis/meetings";
import type { MeetingListItem } from "@/types/meeting";

export const MEETING_LIST_QUERY_KEY = ["meetings"] as const;

export const useMeetingList = (teamId: number | null) => {
  const ongoingQuery = useQuery<MeetingListItem[]>({
    queryKey: [...MEETING_LIST_QUERY_KEY, teamId, "ONGOING"],
    queryFn: () => {
      if (teamId == null) throw new Error("Team ID is required");
      return listMeetings(teamId, "ONGOING");
    },
    enabled: teamId != null,
    refetchInterval: 60_000,
    refetchIntervalInBackground: false,
  });

  const completedQuery = useQuery<MeetingListItem[]>({
    queryKey: [...MEETING_LIST_QUERY_KEY, teamId, "COMPLETED"],
    queryFn: () => {
      if (teamId == null) throw new Error("Team ID is required");
      return listMeetings(teamId, "COMPLETED");
    },
    enabled: teamId != null,
  });

  return {
    ongoing: ongoingQuery.data ?? [],
    completed: completedQuery.data ?? [],
    isLoading: ongoingQuery.isLoading || completedQuery.isLoading,
    isError: ongoingQuery.isError || completedQuery.isError,
    isFetching: ongoingQuery.isFetching || completedQuery.isFetching,
    refetch: () => {
      ongoingQuery.refetch();
      completedQuery.refetch();
    },
  };
};
