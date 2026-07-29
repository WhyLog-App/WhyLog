export type DecisionType = "success" | "warning" | "neutral";

export interface GitHubTokenRequest {
  access_token: string;
}

export interface GitHubTokenResult {
  access_token: string;
}

export interface GitRepositoryStats {
  commits: number;
  connected: number;
  disconnected: number;
}

export interface GitCommitItem {
  id: string;
  hash: string;
  message: string;
  decisionText: string;
  decisionType: DecisionType;
  authorName: string;
  authorProfileImage?: string;
  dateText: string;
  changesAdded: number;
  changesRemoved: number;
}

export interface GitCommitDetailFileLine {
  type: "context" | "added" | "removed";
  content: string;
  lineNumber?: number;
}

export interface GitCommitDetailFile {
  fileName: string;
  addedLines: number;
  removedLines: number;
  lines: GitCommitDetailFileLine[];
}

export interface GitCommitDetailItem {
  repositoryName: string;
  hash: string;
  message: string;
  description: string;
  authorName: string;
  authorEmail: string;
  authorProfileImage?: string;
  dateText: string;
  decisionText: string;
  decisionType: DecisionType;
  changesAdded: number;
  changesRemoved: number;
  files: GitCommitDetailFile[];
}

export interface RepositoryCommitConnectedApplication {
  application_id: number;
  name: string;
}

export interface RepositoryCommitItem {
  commit_id: number;
  hash: string;
  message: string;
  author_name: string;
  author_profile_image: string;
  date_time: string;
  added_lines: number;
  deleted_lines: number;
  connected_application: RepositoryCommitConnectedApplication | null;
}

export interface RepositoryCommitListResult {
  commit_dtolist: RepositoryCommitItem[];
  commit_list_size: number;
  total_commit_count: number;
  connected_commit_count: number;
  unconnected_commit_count: number;
  is_first: boolean;
  has_next: boolean;
  next_cursor_id: number | null;
}

export interface GitPageData {
  repositoryName: string;
  stats: GitRepositoryStats;
  commits: GitCommitItem[];
}

export interface AddRepositoryRequest {
  name: string;
  url: string;
}

export interface AddRepositoryResult {
  repository_id: number;
  name: string;
  url: string;
}

export interface RepositorySyncResult {
  repository_id: number;
}

export interface CommitDetailFileResult {
  file_name: string;
  added_lines: number;
  deleted_lines: number;
  changed_code: string;
}

export interface CommitDetailResult {
  commit_id: number;
  hash: string;
  message: string;
  description: string | null;
  author_name: string;
  author_email: string;
  author_profile_image?: string;
  date_time: string;
  changed_file_count: number;
  changed_file_list: CommitDetailFileResult[];
}

export interface RepositoryItem {
  repository_id: number;
  name: string;
  url?: string;
  last_sync_date_time?: string;
}

export interface CheckGitHubTokenStatusResult {
  is_registered: boolean;
}
