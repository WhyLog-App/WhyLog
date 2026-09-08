import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { updateMyName } from "@/apis/members";
import { patchMemberIdentityCache } from "@/hooks/memberCache";
import { mypageErrorMessage } from "../utils/errorMessage";

export const useUpdateMyName = (memberId: number) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: updateMyName,
    onSuccess: (result) => {
      patchMemberIdentityCache(queryClient, memberId, {
        name: result.name,
        email: result.email,
        profile_image: result.profile_image,
      });
      setSuccessMessage("이름을 저장했습니다.");
    },
    onError: (error: unknown) => {
      setSuccessMessage(null);
      setErrorMessage(mypageErrorMessage(error, "이름 저장에 실패했습니다."));
    },
  });

  return {
    setClientError: (message: string) => {
      setSuccessMessage(null);
      setErrorMessage(message);
    },
    updateName: (name: string) => {
      setErrorMessage(null);
      setSuccessMessage(null);
      mutation.mutate({ name });
    },
    isPending: mutation.isPending,
    errorMessage,
    successMessage,
  };
};
