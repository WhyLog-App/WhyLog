export interface CreateTeamRequest {
  name: string;
  image?: File;
}

export interface CreateTeamResult {
  team_id: number;
  name: string;
  team_image?: string | null;
  image_url?: string | null;
}

export interface Team {
  team_id: number;
  name: string;
  team_image: string | null;
}

export interface InviteTeamMemberRequest {
  member_email: string;
}

export interface InviteTeamMemberResult {
  team_id: number;
  member_email: string;
}

export interface DeleteTeamResult {
  is_removed: boolean;
}

export interface DecisionApplicationItem {
  application_id: number;
  name: string;
}

export interface DecisionListItem {
  decision_id: number;
  name: string;
  applications: DecisionApplicationItem[];
}
