import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { uploadMemberProfileImage } from "@/apis/auth";
import type { ApiResponse } from "@/types/auth";

export const PROFILE_IMAGE_STORAGE_KEY = "profile_image_url";

/**
 * 프로필 이미지 업로드 처리.
 * - 성공: 반환된 profile_image_url을 localStorage에 저장하고 uploadedUrl로 노출
 * - 실패: 에러 메시지를 hook에서 노출
 *
 * 멤버 프로필 조회 GET API가 없으므로, 마지막 업로드 결과를 localStorage에 저장해
 * 새로고침 후에도 유지한다.
 */
export const useUploadProfileImage = () => {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [uploadedUrl, setUploadedUrl] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (image: File) => uploadMemberProfileImage(image),
    onSuccess: (result) => {
      const url = result.profile_image_url;
      setUploadedUrl(url);
      try {
        localStorage.setItem(PROFILE_IMAGE_STORAGE_KEY, url);
      } catch {
        // localStorage 접근 불가 시 무시 (세션 내 표시는 유지)
      }
    },
    onError: (err: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(err)) {
        setErrorMessage(
          err.response?.data?.message ?? "프로필 이미지 업로드에 실패했습니다.",
        );
        return;
      }
      setErrorMessage("프로필 이미지 업로드에 실패했습니다.");
    },
  });

  return {
    uploadProfileImage: (image: File) => {
      setErrorMessage(null);
      mutation.mutate(image);
    },
    isPending: mutation.isPending,
    errorMessage,
    uploadedUrl,
  };
};
