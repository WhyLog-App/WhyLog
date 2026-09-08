const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? "").replace(
  /\/+$/,
  "",
);

const ENDPOINT = {
  TEST: `${API_BASE_URL}/test`,
  AUTH: {
    LOGIN: `${API_BASE_URL}/api/auth/login`,
    SIGNUP: `${API_BASE_URL}/api/auth/signup`,
    REFRESH_TOKEN: `${API_BASE_URL}/api/auth/refresh-token`,
    LOGOUT: `${API_BASE_URL}/api/auth/logout`,
    EMAIL_VERIFICATIONS: `${API_BASE_URL}/api/auth/email-verifications`,
    EMAIL_VERIFICATION_VERIFY: `${API_BASE_URL}/api/auth/email-verifications/verify`,
    WITHDRAWAL_RECOVERY_VERIFY: `${API_BASE_URL}/api/auth/withdrawal-recoveries/verify`,
  },
  TEAMS: {
    LIST: `${API_BASE_URL}/api/members/teams`,
    CREATE: `${API_BASE_URL}/api/teams`,
    DELETE: (teamId: number) => `${API_BASE_URL}/api/teams/${teamId}`,
    REPOSITORIES: (teamId: number) =>
      `${API_BASE_URL}/api/teams/${teamId}/repositories`,
    INVITE: (teamId: number) =>
      `${API_BASE_URL}/api/teams/${teamId}/invitations`,
    DECISIONS: (teamId: number) =>
      `${API_BASE_URL}/api/teams/${teamId}/decisions`,
  },
  MEMBERS: {
    ME_PROFILE: `${API_BASE_URL}/api/members/me/profile`,
    PROFILE: (memberId: number) =>
      `${API_BASE_URL}/api/members/${memberId}/profile`,
    PROJECTS: (memberId: number) =>
      `${API_BASE_URL}/api/members/${memberId}/projects`,
    ME_NAME: `${API_BASE_URL}/api/members/me/name`,
    ME_PROFILE_VISIBILITY: `${API_BASE_URL}/api/members/me/profile/visibility`,
    ME_PROFILE_IMAGE: `${API_BASE_URL}/api/members/me/profile-image`,
    ME_PASSWORD: `${API_BASE_URL}/api/members/me/password`,
    ME_PASSWORD_VERIFY: `${API_BASE_URL}/api/members/me/password/verify`,
    ME_WITHDRAWAL: `${API_BASE_URL}/api/members/me/withdrawal`,
  },
  MEETINGS: {
    CREATE: (teamId: number) => `${API_BASE_URL}/api/teams/${teamId}/meetings`,
    LIST: (teamId: number) => `${API_BASE_URL}/api/teams/${teamId}/meetings`,
    DETAIL: (meetingId: number) => `${API_BASE_URL}/api/meetings/${meetingId}`,
    DELETE: (meetingId: number) => `${API_BASE_URL}/api/meetings/${meetingId}`,
    RTC_TOKEN: (meetingId: number) =>
      `${API_BASE_URL}/api/meetings/${meetingId}/rtc-token`,
    END: (meetingId: number) => `${API_BASE_URL}/api/meetings/${meetingId}/end`,
    ANALYSIS: (meetingId: number) =>
      `${API_BASE_URL}/api/meetings/${meetingId}/analysis`,
    AUDIO: (meetingId: number) =>
      `${API_BASE_URL}/api/meetings/${meetingId}/audio`,
    HISTORY: (meetingId: number) =>
      `${API_BASE_URL}/api/meetings/${meetingId}/history`,
  },
  DECISIONS: {
    RELIABILITY: (decisionId: number) =>
      `${API_BASE_URL}/api/decisions/${decisionId}/reliability`,
    MATCH_COMMIT: (decisionId: number) =>
      `${API_BASE_URL}/api/decisions/${decisionId}/commit/match`,
  },
  APPLICATIONS: {
    DETAIL: (applicationId: number) =>
      `${API_BASE_URL}/api/applications/${applicationId}`,
    RECOMMENDED_COMMITS: (applicationId: number) =>
      `${API_BASE_URL}/api/applications/${applicationId}/recommended-commits`,
    CONNECTED_COMMITS: (applicationId: number) =>
      `${API_BASE_URL}/api/applications/${applicationId}/connected-commits`,
    LINK_COMMIT: (applicationId: number) =>
      `${API_BASE_URL}/api/applications/${applicationId}/commits`,
  },
  GIT: {
    GITHUB_TOKEN: `${API_BASE_URL}/api/github/token`,
    GITHUB_TOKEN_STATUS: `${API_BASE_URL}/api/github/token/status`,
    COMMITS: (repositoryId: number) =>
      `${API_BASE_URL}/api/repositories/${repositoryId}/commits`,
    COMMIT_DETAIL: (repositoryId: number, commitHash: string) =>
      `${API_BASE_URL}/api/repositories/${repositoryId}/commits/${encodeURIComponent(commitHash)}`,
    REPOSITORY_SYNC: (repositoryId: number) =>
      `${API_BASE_URL}/api/repositories/${repositoryId}/sync`,
  },
} as const;

export const WS_BASE_URL = API_BASE_URL.replace(/^http/i, "ws");

export default ENDPOINT;
