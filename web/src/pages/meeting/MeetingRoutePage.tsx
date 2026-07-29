import { useQueryClient } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import type { MeetingListItem } from "@/types/meeting";
import { parseRouteId } from "@/utils/parseRouteId";
import CompletedPage from "./CompletedPage";
import { useMeetingDetail } from "./hooks/useMeetingDetail";
import { MEETING_LIST_QUERY_KEY } from "./hooks/useMeetingList";
import InProgressPage from "./InProgressPage";

const MeetingRoutePage = () => {
  const { meetingId: meetingIdParam } = useParams<{ meetingId: string }>();
  const meetingId = parseRouteId(meetingIdParam);
  const { teamId } = useCurrentTeam();

  const queryClient = useQueryClient();

  // 1) 사이드바가 이미 로드해 둔 캐시에서 status 확인
  const cachedOngoing =
    teamId != null
      ? queryClient.getQueryData<MeetingListItem[]>([
          ...MEETING_LIST_QUERY_KEY,
          teamId,
          "ONGOING",
        ])
      : undefined;
  const cachedCompleted =
    teamId != null
      ? queryClient.getQueryData<MeetingListItem[]>([
          ...MEETING_LIST_QUERY_KEY,
          teamId,
          "COMPLETED",
        ])
      : undefined;

  const inOngoing =
    cachedOngoing?.some((m) => m.meeting_id === meetingId) ?? false;
  const inCompleted =
    cachedCompleted?.some((m) => m.meeting_id === meetingId) ?? false;

  const cacheResolved = inOngoing || inCompleted;

  // 2) fallback: 캐시로 결정 못 한 경우에만 detail 조회
  const {
    data: detail,
    isLoading,
    isError,
  } = useMeetingDetail(meetingId, { enabled: !cacheResolved });

  if (meetingId == null || isError) {
    return (
      <div className="flex h-full items-center justify-center text-(--color-text-secondary)">
        회의를 찾을 수 없습니다.
      </div>
    );
  }

  if (inOngoing) return <InProgressPage />;
  if (inCompleted) return <CompletedPage />;

  if (detail) {
    return detail.end_date_time ? <CompletedPage /> : <InProgressPage />;
  }

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center text-(--color-text-secondary)">
        회의 정보를 불러오는 중...
      </div>
    );
  }

  return null;
};

export default MeetingRoutePage;
