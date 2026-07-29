import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { registerGitHubToken } from "@/apis/git";
import type { ApiResponse } from "@/types/auth";
import type {
  CheckGitHubTokenStatusResult,
  GitHubTokenResult,
} from "@/types/git";

interface UseRegisterGitHubTokenOptions {
  onSuccess?: (result: GitHubTokenResult) => void;
}

export const useRegisterGitHubToken = (
  options?: UseRegisterGitHubTokenOptions,
) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (accessToken: string) => registerGitHubToken(accessToken),
    onSuccess: (result: GitHubTokenResult) => {
      queryClient.setQueryData<CheckGitHubTokenStatusResult>(
        ["github-token-status"],
        { is_registered: true },
      );
      options?.onSuccess?.(result);
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "토큰 등록에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }

      if (error instanceof Error) {
        setErrorMessage(error.message);
        return;
      }

      setErrorMessage("토큰 등록에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    registerGitHubToken: async (accessToken: string): Promise<boolean> => {
      setErrorMessage(null);

      try {
        await mutation.mutateAsync(accessToken);
        return true;
      } catch {
        return false;
      }
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
