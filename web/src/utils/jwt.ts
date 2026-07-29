export interface AccessTokenPayload {
  memberId: number;
  role: string;
  iat: number;
  exp: number;
}

export const decodeAccessToken = (token: string): AccessTokenPayload | null => {
  try {
    const payload = token.split(".")[1];
    if (!payload) return null;
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json) as AccessTokenPayload;
  } catch {
    return null;
  }
};
