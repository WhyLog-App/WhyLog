export const ROUTES = {
  APP_ROOT: "/",
  LANDING: "/landing",
  SIGNUP: "/signup",
  LOGIN: "/login",

  // 온보딩
  ONBOARDING_CREATE_TEAM: "/onboarding/create-team",

  // 팀 기반 라우트
  TEAM_ROOT: "/team/:teamId",
  TEAM_HOME: "/team/:teamId",
  TEAM_DECISIONS: "/team/:teamId/decisions",
  TEAM_DECISION_APPLICATION_DETAIL:
    "/team/:teamId/decisions/:decisionId/:applicationId",
  TEAM_MEETING: "/team/:teamId/meeting",
  TEAM_MEETING_DETAIL: "/team/:teamId/meeting/:meetingId",
  TEAM_GIT: "/team/:teamId/git",
  TEAM_SETTINGS: "/team/:teamId/settings",

  // 레거시 라우트 (하위 호환성)
  DECISIONS: "/decisions",
  MEETING: "/meeting",
  MEETING_DETAIL: "/meeting/:meetingId",
  GIT: "/git",
} as const;

// 헬퍼 함수: 팀 ID와 경로를 받아 완전한 URL 생성
export const createTeamRoute = (teamId: number, path: string = "") => {
  return `/team/${teamId}${path}`;
};
