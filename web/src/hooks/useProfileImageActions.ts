import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { removeMyProfileImage, uploadMemberProfileImage } from "@/apis/members";
import { patchMemberIdentityCache } from "@/hooks/memberCache";
import type { ApiResponse } from "@/types/auth";

const profileImageErrorMessage = (error: unknown, fallback: string) => {
  if (isAxiosError<ApiResponse<unknown>>(error)) {
    return error.response?.data?.message ?? fallback;
  }
  return fallback;
};

const logProfileImageRequestFailure = (context: string, error: unknown) => {
  console.error(context, {
    status: isAxiosError(error) ? error.response?.status : undefined,
  });
};

export const useProfileImageActions = (memberId: number | null) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const uploadMutation = useMutation({
    mutationFn: uploadMemberProfileImage,
    onSuccess: (result) => {
      if (memberId != null) {
        patchMemberIdentityCache(queryClient, memberId, {
          profile_image: result.profile_image_url,
        });
      }
    },
    onError: (error: unknown) => {
      setErrorMessage(
        profileImageErrorMessage(error, "프로필 이미지 저장에 실패했습니다."),
      );
    },
  });

  const removeMutation = useMutation({
    mutationFn: removeMyProfileImage,
    onSuccess: (result) => {
      if (memberId != null) {
        patchMemberIdentityCache(queryClient, memberId, {
          profile_image: result.profile_image,
        });
      }
    },
    onError: (error: unknown) => {
      setErrorMessage(
        profileImageErrorMessage(error, "프로필 이미지 삭제에 실패했습니다."),
      );
    },
  });

  return {
    uploadImage: async (image: File) => {
      if (memberId == null) {
        setErrorMessage("프로필 정보를 불러온 뒤 다시 시도해 주세요.");
        return false;
      }

      setErrorMessage(null);
      try {
        await uploadMutation.mutateAsync(image);
        return true;
      } catch (error) {
        logProfileImageRequestFailure("프로필 이미지 저장 요청 실패", error);
        return false;
      }
    },
    removeImage: async () => {
      if (memberId == null) {
        setErrorMessage("프로필 정보를 불러온 뒤 다시 시도해 주세요.");
        return false;
      }

      setErrorMessage(null);
      try {
        await removeMutation.mutateAsync();
        return true;
      } catch (error) {
        logProfileImageRequestFailure("프로필 이미지 삭제 요청 실패", error);
        return false;
      }
    },
    isPending: uploadMutation.isPending || removeMutation.isPending,
    errorMessage,
  };
};
