import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { addRepository } from "@/apis/git";
import type { AddRepositoryRequest, AddRepositoryResult } from "@/types/git";

interface UseAddRepositoryOptions {
  onSuccess?: (result: AddRepositoryResult) => void;
  onTokenExpired?: () => void;
}

interface ErrorResponse {
  message?: string;
  code?: string;
}

export const useAddRepository = (options?: UseAddRepositoryOptions) => {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: ({
      teamId,
      payload,
    }: {
      teamId: number;
      payload: AddRepositoryRequest;
    }) => addRepository(teamId, payload),
    onSuccess: (result: AddRepositoryResult) => {
      options?.onSuccess?.(result);
    },
    onError: (error: unknown) => {
      if (isAxiosError<ErrorResponse>(error)) {
        if (
          error.response?.data?.code === "GIT_401_1" ||
          error.response?.data?.code === "GIT_400_2"
        ) {
          queryClient.setQueryData(["github-token-status"], {
            is_registered: false,
          });
          options?.onTokenExpired?.();
          return;
        }

        const message = error.response?.data?.message;
        setErrorMessage(
          message ?? "레포지토리 추가에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }

      if (error instanceof Error) {
        setErrorMessage(error.message);
        return;
      }

      setErrorMessage("레포지토리 추가에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    addRepository: (teamId: number, payload: AddRepositoryRequest) => {
      setErrorMessage(null);
      mutation.mutate({ teamId, payload });
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};

export default useAddRepository;
