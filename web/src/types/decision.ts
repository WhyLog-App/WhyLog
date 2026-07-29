// GET /api/decisions/{decisionId}
export interface DecisionTimelineItem {
  time: string;
  step: string;
  content: string;
}

export interface DecisionContextMessage {
  time: string;
  member_id: number;
  member_name: string;
  profile_image: string;
  dialogue_content: string;
}

export interface DecisionReasonItem {
  reason_id: string;
  title: string;
}

export interface DecisionDetail {
  application_id: number;
  name: string;
  decision_timelines: DecisionTimelineItem[];
  decision_contexts: DecisionContextMessage[];
  decision_reason_count: number;
  decision_reasons: DecisionReasonItem[];
}

// GET /api/decisions/{decisionId}/applications/{applicationId}
export interface DecisionApplicationDetail {
  application_id: number;
  repository_name: string;
  commit_id: string;
  commit_hash: string;
  message: string;
  reason: string;
}

// =============================================================================
// TODO: BE 스키마에 아직 없는 필드 — 추후 위 인터페이스로 흡수.
// 디자인에 표현돼 있어 mock 단계에서만 사용하는 view-model 확장입니다.
// =============================================================================

export interface DecisionMeetingMeta {
  meeting_name: string;
  meeting_date: string; // "YYYY.MM.DD"
  duration_label: string; // "회의 시간 47분"
  participant_count: number;
  participants: {
    member_id: number;
    name: string;
    profile_image: string;
  }[];
}

export interface DecisionConfidence {
  score: number | null;
}

export interface DecisionFooterStats {
  evidence_utterance_count: number;
  participant_consensus_label: string;
  decision_implementation_match_rate: number; // 0–100
}

export type CommitTab = "recommended" | "linked";

// 페이지가 소비하는 통합 view-model
import type {
  ApplicationConnectedCommit,
  ApplicationRecommendedCommit,
} from "./application";

export interface DecisionDetailViewModel {
  detail: DecisionDetail;
  application: DecisionApplicationDetail;
  meta: DecisionMeetingMeta;
  confidence: DecisionConfidence;
  applied_commits: DecisionApplicationDetail[];
  recommended_commits: ApplicationRecommendedCommit[];
  linked_commits: ApplicationConnectedCommit[];
}
