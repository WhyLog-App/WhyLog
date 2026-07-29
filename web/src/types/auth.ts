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

export type UserRole = "ROLE_USER" | "ROLE_ADMIN" | string;

export interface LoginResult {
  access_token: string;
  refresh_token: string;
  member_id: number;
  email: string;
  role: UserRole;
}

export interface SignupResult {
  access_token: string;
  refresh_token: string;
  member_id: number;
  email: string;
  role: UserRole;
}

export interface ProfileImageUploadResult {
  member_id: number;
  profile_image_url: string;
}

export interface RefreshTokenResult {
  access_token: string;
}
