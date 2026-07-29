import type { DecisionDetailViewModel } from "@/types/decision";

const avatar = (seed: string) => `https://i.pravatar.cc/56?u=${seed}`;

export const MOCK_DECISION_DETAIL_VIEW_MODEL: DecisionDetailViewModel = {
  detail: {
    application_id: 1,
    name: "Redis 키 정책 변경",
    decision_timelines: [
      { time: "12:24", step: "이슈제기", content: "장애 이슈 제기" },
      { time: "12:26", step: "논의", content: "Redis vs Mongo 논의" },
      { time: "12:28", step: "결정", content: "최종 결정" },
    ],
    decision_contexts: [
      {
        time: "12:24",
        member_id: 1,
        member_name: "김준용",
        profile_image: avatar("kim"),
        dialogue_content:
          "Redis 키 만료 정책이 지금 TTL 고정인데, 세션 유형별로 다르게 가져가야 할 것 같아요. 지금 인증 세션이랑 캐시 세션이 같은 TTL인 게 문제인 거죠.",
      },
      {
        time: "12:26",
        member_id: 2,
        member_name: "유진",
        profile_image: avatar("yujin"),
        dialogue_content:
          "Mongo로 옮기자는 의견도 있었는데, 운영 복잡도 올라가니까 Redis 유지하면서 키 네이밍 규칙만 바꾸는 게 낫지 않을까요?",
      },
      {
        time: "12:27",
        member_id: 3,
        member_name: "유상완",
        profile_image: avatar("yoo"),
        dialogue_content:
          "저도 Redis 유지에 동의합니다. 메모리 비용 이슈는 키 정책 분리로 충분히 대응 가능할 것 같아요.",
      },
      {
        time: "12:28",
        member_id: 4,
        member_name: "조윤지",
        profile_image: avatar("cho"),
        dialogue_content:
          "좋습니다. 그럼 Redis 유지하고 키 정책만 세션 유형별로 분리하는 걸로 가겠습니다.",
      },
    ],
    decision_reason_count: 2,
    decision_reasons: [
      { reason_id: "1", title: "운영 복잡도 우려로 보류" },
      { reason_id: "2", title: "Redis 메모리 비용" },
      { reason_id: "3", title: "Redis 메모리 비용" },
    ],
  },
  application: {
    application_id: 1,
    repository_name: "whyLog-Backend",
    commit_id: "1",
    commit_hash: "b8fd9ad",
    message: "feat: API 구현",
    reason: "이 커밋은 관련된 이슈를 해결하는 커밋입니다.",
  },
  meta: {
    meeting_name: "서버 개발 팀 4차 회의",
    meeting_date: "2025.01.20",
    duration_label: "회의 시간 47분",
    participant_count: 5,
    participants: [
      { member_id: 1, name: "김준용", profile_image: avatar("kim") },
      { member_id: 2, name: "유진", profile_image: avatar("yujin") },
      { member_id: 3, name: "유상완", profile_image: avatar("yoo") },
      { member_id: 4, name: "조윤지", profile_image: avatar("cho") },
      { member_id: 5, name: "이도현", profile_image: avatar("lee") },
    ],
  },
  confidence: { score: 90 },
  applied_commits: [
    {
      application_id: 1,
      repository_name: "WhyLog-Backend",
      commit_id: "1",
      commit_hash: "a13f9c2",
      message: "feat: 세션 저장소 인터페이스 분리",
      reason: "",
    },
    {
      application_id: 2,
      repository_name: "WhyLog-Backend",
      commit_id: "2",
      commit_hash: "a13f9c2",
      message: "feat: 세션 저장소 인터페이스 분리",
      reason: "",
    },
    {
      application_id: 3,
      repository_name: "WhyLog-Backend",
      commit_id: "3",
      commit_hash: "a13f9c2",
      message: "refactor: 마이페이지 조회 성능 개선",
      reason: "",
    },
  ],
  recommended_commits: [],
  linked_commits: [],
};

/**
 * TODO: API 연동 시 `useDecisionDetail(decisionId)` 와
 * `useDecisionApplication(applicationId)` 두 개의 React Query 훅으로 교체.
 * 현재는 id 무관하게 단일 mock 을 반환합니다.
 */
export const getMockDecisionDetail = (
  _decisionId: number,
  _applicationId: number,
): DecisionDetailViewModel => MOCK_DECISION_DETAIL_VIEW_MODEL;
