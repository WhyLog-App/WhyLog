import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { syncRepository } from "@/apis/git";
import type { RepositorySyncResult } from "@/types/git";

interface UseSyncRepositoryOptions {
  onSuccess?: (result: RepositorySyncResult) => void;
  onTokenExpired?: () => void;
}

interface ErrorResponse {
  message?: string;
  code?: string;
}

export const useSyncRepository = (options?: UseSyncRepositoryOptions) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (repositoryId: number) => syncRepository(repositoryId),
    onSuccess: (result: RepositorySyncResult) => {
      options?.onSuccess?.(result);
    },
    onError: (error: unknown) => {
      if (isAxiosError<ErrorResponse>(error)) {
        if (error.response?.data?.code === "GIT_401_1") {
          queryClient.setQueryData(["github-token-status"], {
            is_registered: false,
          });
          options?.onTokenExpired?.();
          return;
        }

        const message = error.response?.data?.message;
        setErrorMessage(
          message ?? "레포지토리 동기화에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }

      if (error instanceof Error) {
        setErrorMessage(error.message);
        return;
      }

      setErrorMessage("레포지토리 동기화에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    syncRepository: async (repositoryId: number): Promise<boolean> => {
      setErrorMessage(null);

      try {
        await mutation.mutateAsync(repositoryId);
        return true;
      } catch {
        return false;
      }
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};

export default useSyncRepository;
