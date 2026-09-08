export interface ApiResponse<T> {
  isSuccess: boolean;
  code: string;
  message: string;
  result: T;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
}

export interface EmailVerificationIssueRequest {
  email: string;
}

export interface EmailVerificationVerifyRequest {
  email: string;
  code: string;
}

export interface WithdrawalRecoveryVerifyRequest {
  member_id: number;
  challenge: string;
}

export type UserRole = "ROLE_USER" | "ROLE_ADMIN" | string;
export type LoginStatus =
  | "AUTHENTICATED"
  | "RECOVERY_REQUIRED"
  | "EMAIL_VERIFICATION_REQUIRED";

export interface LoginResult {
  status: LoginStatus;
  access_token: string | null;
  member_id: number;
  email: string;
  role: UserRole;
  withdrawal_recovery_challenge: string | null;
  purge_at: string | null;
}

export interface SignupResult {
  member_id: number;
  email: string;
}

export interface RefreshTokenResult {
  access_token: string;
}
