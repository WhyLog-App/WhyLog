import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { unlinkCommit } from "@/apis/applications";
import type { ApiResponse } from "@/types/auth";
import { APPLICATION_CONNECTED_COMMITS_QUERY_KEY } from "./useConnectedCommits";
import { APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY } from "./useRecommendedCommits";

interface UseUnlinkCommitOptions {
  onSuccess?: () => void;
}

export const useUnlinkCommit = (
  applicationId: number,
  options?: UseUnlinkCommitOptions,
) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (commitId: number) =>
      unlinkCommit(applicationId, { commit_id: commitId }),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: [
            ...APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY,
            applicationId,
          ],
        }),
        queryClient.invalidateQueries({
          queryKey: [...APPLICATION_CONNECTED_COMMITS_QUERY_KEY, applicationId],
        }),
      ]);
      options?.onSuccess?.();
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "커밋 연결 해제에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("커밋 연결 해제에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    unlinkCommit: (commitId: number) => {
      setErrorMessage(null);
      mutation.mutate(commitId);
    },
    pendingCommitId: mutation.isPending ? mutation.variables : null,
    isPending: mutation.isPending,
    errorMessage,
    resetError: () => setErrorMessage(null),
  };
};
