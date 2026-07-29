// GET /api/applications/{applicationId}
export interface ApplicationTimelineItem {
  time: string;
  step: string;
  content: string;
}

export interface ApplicationContextMessage {
  time: string;
  member_id: number;
  member_name: string;
  profile_image: string;
  dialogue_content: string;
}

export interface ApplicationReasonItem {
  reason_id: string;
  title: string;
}

export interface ApplicationDetail {
  application_id: number;
  meeting_id: number;
  name: string;
  decision_timelines: ApplicationTimelineItem[];
  decision_contexts: ApplicationContextMessage[];
  decision_reason_count: number;
  decision_reasons: ApplicationReasonItem[];
}

// GET /api/applications/{applicationId}/recommended-commits
export interface ApplicationRecommendedCommit {
  repository_name: string;
  commit_id: string;
  commit_hash: string;
  message: string;
  reason: string;
}

// GET /api/applications/{applicationId}/connected-commits
export interface ApplicationConnectedCommit {
  repository_name: string;
  commit_id: number;
  commit_hash: string;
  message: string;
  committed_date: string;
}

export interface ApplicationConnectedCommitsResult {
  commit_count: number;
  commits: ApplicationConnectedCommit[];
}

// POST /api/applications/{applicationId}/commits
export interface LinkCommitRequest {
  commit_ids: number[];
}

export interface LinkCommitResult {
  application_id: number;
  commit_ids: number[];
}

// DELETE /api/applications/{applicationId}/commits
export interface UnlinkCommitRequest {
  commit_id: number;
}

export interface UnlinkCommitResult {
  application_id: number;
  commit_ids: number[];
}
