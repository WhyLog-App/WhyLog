// In-memory access token storage - XSS로부터 안전
let accessToken: string | null = null;

export const tokenStore = {
  getToken(): string | null {
    return accessToken;
  },

  setToken(token: string): void {
    accessToken = token;
  },

  clearToken(): void {
    accessToken = null;
  },

  hasToken(): boolean {
    return accessToken !== null;
  },
};
