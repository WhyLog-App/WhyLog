import ENDPOINT from "@/constants/endpoint";
import type { ApiResponse } from "@/types/auth";
import type {
  ChangePasswordRequest,
  MemberProfile,
  MemberUpdateResult,
  MyInfo,
  ParticipatingProjectListResult,
  PasswordVerifyRequest,
  ProfileImageUploadResult,
  ProfileVisibilityUpdateResult,
  UpdateNameRequest,
  UpdateProfileVisibilityRequest,
} from "@/types/member";
import { http } from "@/utils/http";

export const getMyInfo = async (): Promise<MyInfo> => {
  const { data } = await http.get<unknown, { data: ApiResponse<MyInfo> }>(
    ENDPOINT.MEMBERS.ME_PROFILE,
  );
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getMemberProfile = async (
  memberId: number,
): Promise<MemberProfile> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<MemberProfile> }
  >(ENDPOINT.MEMBERS.PROFILE(memberId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const getMemberProjects = async (
  memberId: number,
  cursor?: number,
): Promise<ParticipatingProjectListResult> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<ParticipatingProjectListResult> }
  >(ENDPOINT.MEMBERS.PROJECTS(memberId), {
    params: cursor == null ? undefined : { cursor },
  });
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const updateMyName = async (
  payload: UpdateNameRequest,
): Promise<MemberUpdateResult> => {
  const { data } = await http.patch<
    UpdateNameRequest,
    { data: ApiResponse<MemberUpdateResult> }
  >(ENDPOINT.MEMBERS.ME_NAME, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const updateMyProfileVisibility = async (
  payload: UpdateProfileVisibilityRequest,
): Promise<ProfileVisibilityUpdateResult> => {
  const { data } = await http.patch<
    UpdateProfileVisibilityRequest,
    { data: ApiResponse<ProfileVisibilityUpdateResult> }
  >(ENDPOINT.MEMBERS.ME_PROFILE_VISIBILITY, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const removeMyProfileImage = async (): Promise<MemberUpdateResult> => {
  const { data } = await http.delete<
    unknown,
    { data: ApiResponse<MemberUpdateResult> }
  >(ENDPOINT.MEMBERS.ME_PROFILE_IMAGE);
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
  >(ENDPOINT.MEMBERS.ME_PROFILE_IMAGE, formData);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const verifyMyPassword = async (
  payload: PasswordVerifyRequest,
): Promise<void> => {
  const { data } = await http.post<
    PasswordVerifyRequest,
    { data: ApiResponse<null> }
  >(ENDPOINT.MEMBERS.ME_PASSWORD_VERIFY, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
};

export const changeMyPassword = async (
  payload: ChangePasswordRequest,
): Promise<MemberUpdateResult> => {
  const { data } = await http.patch<
    ChangePasswordRequest,
    { data: ApiResponse<MemberUpdateResult> }
  >(ENDPOINT.MEMBERS.ME_PASSWORD, payload);
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

export const requestMyWithdrawal = async (): Promise<void> => {
  const { data } = await http.post<undefined, { data: ApiResponse<null> }>(
    ENDPOINT.MEMBERS.ME_WITHDRAWAL,
  );
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
};
