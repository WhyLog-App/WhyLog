import { useParams } from "react-router-dom";
import { useMeetingDetail } from "@/pages/meeting/hooks/useMeetingDetail";
import type {
  DecisionDetailViewModel,
  DecisionMeetingMeta,
} from "@/types/decision";
import type { MeetingDetail } from "@/types/meeting";
import { formatCommittedDate } from "@/utils/date";
import { parseRouteId } from "@/utils/parseRouteId";
import DecisionDetailPage from "./DecisionDetailPage";
import { useApplicationDetail } from "./hooks/useApplicationDetail";
import { useConnectedCommits } from "./hooks/useConnectedCommits";
import { useDecisionReliability } from "./hooks/useDecisionReliability";
import { useRecommendedCommits } from "./hooks/useRecommendedCommits";
import DecisionsPage from "./index";
import { MOCK_DECISION_DETAIL_VIEW_MODEL } from "./mocks";

const toMeetingMeta = (m: MeetingDetail): DecisionMeetingMeta => ({
  meeting_name: m.name,
  meeting_date: formatCommittedDate(m.start_date_time),
  duration_label:
    m.duration != null ? `회의 시간 ${m.duration}분` : "회의 시간 -",
  participant_count: m.member_count,
  participants: m.members.map((p) => ({
    member_id: p.member_id,
    name: p.name,
    profile_image: p.profile_image ?? "",
  })),
});

const DecisionsRoutePage = () => {
  const { decisionId: decisionIdParam, applicationId: applicationIdParam } =
    useParams<{ decisionId: string; applicationId: string }>();
  const decisionId = parseRouteId(decisionIdParam);
  const applicationId = parseRouteId(applicationIdParam);

  const detailQuery = useApplicationDetail(applicationId);
  const recommendedQuery = useRecommendedCommits(applicationId);
  const connectedQuery = useConnectedCommits(applicationId);
  const reliabilityQuery = useDecisionReliability(decisionId);
  const meetingQuery = useMeetingDetail(detailQuery.data?.meeting_id ?? null);

  if (decisionId == null || applicationId == null) {
    return <DecisionsPage />;
  }

  const isLoading =
    detailQuery.isLoading ||
    recommendedQuery.isLoading ||
    connectedQuery.isLoading;
  const error =
    detailQuery.error ?? recommendedQuery.error ?? connectedQuery.error;

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center p-10 typo-body5 text-(--color-text-secondary)">
        불러오는 중...
      </div>
    );
  }

  if (error || !detailQuery.data) {
    return (
      <div className="flex h-full items-center justify-center p-10 typo-body5 text-(--color-text-secondary)">
        적용사항을 불러오지 못했습니다.
      </div>
    );
  }

  // TODO: api 연동
  const vm: DecisionDetailViewModel = {
    detail: detailQuery.data,
    application: MOCK_DECISION_DETAIL_VIEW_MODEL.application,
    meta: meetingQuery.data
      ? toMeetingMeta(meetingQuery.data)
      : MOCK_DECISION_DETAIL_VIEW_MODEL.meta,
    confidence:
      reliabilityQuery.data ?? MOCK_DECISION_DETAIL_VIEW_MODEL.confidence,
    applied_commits: MOCK_DECISION_DETAIL_VIEW_MODEL.applied_commits,
    recommended_commits: recommendedQuery.data ?? [],
    linked_commits: connectedQuery.data?.commits ?? [],
  };

  return <DecisionDetailPage vm={vm} />;
};

export default DecisionsRoutePage;
