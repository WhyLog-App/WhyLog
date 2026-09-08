export type AccountStatus = "UNVERIFIED" | "ACTIVE" | "INACTIVE" | "WITHDRAW";
export type ProfileVisibility = "PUBLIC" | "PRIVATE";
export type ProfileView = "FULL" | "PRIVATE" | "WITHDRAWN";

export interface ParticipatingProjectSummary {
  project_id: number;
  name: string;
  image: string | null;
  member_completed_meeting_count: number;
  member_completed_meeting_duration_seconds: number;
  project_stored_commit_count: number;
  last_synced_at: string | null;
}

export interface RecentMeetingSummary {
  meeting_id: number;
  project_id: number;
  project_name: string;
  name: string;
  ended_at: string | null;
  duration_seconds: number | null;
}

export interface RecentDecisionSummary {
  decision_id: number;
  project_id: number;
  project_name: string;
  name: string;
  created_at: string | null;
}

export interface MyInfo {
  member_id: number;
  name: string;
  email: string;
  profile_image: string | null;
  participating_project_count: number;
  account_status: AccountStatus;
  profile_visibility: ProfileVisibility;
  recent_meetings: RecentMeetingSummary[];
  recent_decisions: RecentDecisionSummary[];
}

export interface FullMemberProfile {
  profile_view: "FULL";
  member_id: number;
  name: string;
  email: string;
  profile_image: string | null;
  participating_project_count: number;
}

export interface PrivateMemberProfile {
  profile_view: "PRIVATE";
  member_id: number;
  name: string;
  email: string;
  profile_image: string | null;
}

export interface WithdrawnMemberProfile {
  profile_view: "WITHDRAWN";
  member_id: number;
  name: string;
  email: string;
  profile_image: null;
}

export type MemberProfile =
  | FullMemberProfile
  | PrivateMemberProfile
  | WithdrawnMemberProfile;

export interface ParticipatingProjectListResult {
  participating_projects: ParticipatingProjectSummary[];
  project_list_size: number;
  is_first: boolean;
  has_next: boolean;
  next_cursor_id: number | null;
}

export interface MemberUpdateResult {
  member_id: number;
  name: string;
  email: string;
  profile_image: string | null;
}

export interface ProfileImageUploadResult {
  member_id: number;
  profile_image_url: string;
}

export interface ProfileVisibilityUpdateResult {
  profile_visibility: ProfileVisibility;
}

export interface UpdateNameRequest {
  name: string;
}

export interface UpdateProfileVisibilityRequest {
  profile_visibility: ProfileVisibility;
}

export interface PasswordVerifyRequest {
  current_password: string;
}

export interface ChangePasswordRequest {
  current_password: string;
  new_password: string;
}
