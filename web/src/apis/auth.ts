import ENDPOINT from "@/constants/endpoint";
import type {
  ApiResponse,
  LoginRequest,
  LoginResult,
  ProfileImageUploadResult,
  RefreshTokenResult,
  SignupRequest,
  SignupResult,
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

export const uploadMemberProfileImage = async (
  image: File,
): Promise<ProfileImageUploadResult> => {
  const formData = new FormData();
  formData.append("image", image);

  const { data } = await http.post<
    FormData,
    { data: ApiResponse<ProfileImageUploadResult> }
  >(ENDPOINT.MEMBERS.PROFILE_IMAGE, formData);

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
