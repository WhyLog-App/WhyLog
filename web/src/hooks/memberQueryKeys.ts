export const MY_INFO_QUERY_KEY = ["members", "me", "profile"] as const;

export const MEMBER_PROFILE_QUERY_KEY = ["members", "profile"] as const;

export const memberProfileQueryKey = (memberId: number) =>
  [...MEMBER_PROFILE_QUERY_KEY, memberId] as const;

export const MEMBER_PROJECTS_QUERY_KEY = ["members", "projects"] as const;

export const memberProjectsQueryKey = (memberId: number | null) =>
  [...MEMBER_PROJECTS_QUERY_KEY, memberId] as const;
