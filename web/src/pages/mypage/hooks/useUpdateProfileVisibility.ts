import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { updateMyProfileVisibility } from "@/apis/members";
import { patchOwnProfileVisibilityCache } from "@/hooks/memberCache";
import type { ProfileVisibility } from "@/types/member";
import { mypageErrorMessage } from "../utils/errorMessage";

export const useUpdateProfileVisibility = (memberId: number) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: updateMyProfileVisibility,
    onSuccess: (result) => {
      patchOwnProfileVisibilityCache(
        queryClient,
        memberId,
        result.profile_visibility,
      );
    },
    onError: (error: unknown) => {
      setErrorMessage(
        mypageErrorMessage(error, "프로필 공개 설정 저장에 실패했습니다."),
      );
    },
  });

  return {
    update: (profileVisibility: ProfileVisibility) => {
      setErrorMessage(null);
      mutation.mutate({ profile_visibility: profileVisibility });
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
