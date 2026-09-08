import ENDPOINT from "@/constants/endpoint";
import type {
  ApiResponse,
  EmailVerificationIssueRequest,
  EmailVerificationVerifyRequest,
  LoginRequest,
  LoginResult,
  RefreshTokenResult,
  SignupRequest,
  SignupResult,
  WithdrawalRecoveryVerifyRequest,
} from "@/types/auth";
import { http } from "@/utils/http";

export const login = async (payload: LoginRequest): Promise<LoginResult> => {
  const { data } = await http.post<
    LoginRequest,
    { data: ApiResponse<LoginResult> }
  >(ENDPOINT.AUTH.LOGIN, payload);
  return data.result;
};

export const signup = async (payload: SignupRequest): Promise<SignupResult> => {
  const { data } = await http.post<
    SignupRequest,
    { data: ApiResponse<SignupResult> }
  >(ENDPOINT.AUTH.SIGNUP, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const issueEmailVerification = async (
  payload: EmailVerificationIssueRequest,
): Promise<void> => {
  const { data } = await http.post<
    EmailVerificationIssueRequest,
    { data: ApiResponse<null> }
  >(ENDPOINT.AUTH.EMAIL_VERIFICATIONS, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
};

export const verifyEmailVerification = async (
  payload: EmailVerificationVerifyRequest,
): Promise<LoginResult> => {
  const { data } = await http.post<
    EmailVerificationVerifyRequest,
    { data: ApiResponse<LoginResult> }
  >(ENDPOINT.AUTH.EMAIL_VERIFICATION_VERIFY, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const verifyWithdrawalRecovery = async (
  payload: WithdrawalRecoveryVerifyRequest,
): Promise<LoginResult> => {
  const { data } = await http.post<
    WithdrawalRecoveryVerifyRequest,
    { data: ApiResponse<LoginResult> }
  >(ENDPOINT.AUTH.WITHDRAWAL_RECOVERY_VERIFY, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const refreshAccessToken = async (): Promise<string> => {
  const { data } = await http.post<
    undefined,
    { data: ApiResponse<RefreshTokenResult> }
  >(ENDPOINT.AUTH.REFRESH_TOKEN);
  return data.result.access_token;
};

export const logout = async (): Promise<void> => {
  await http.post(ENDPOINT.AUTH.LOGOUT);
};
