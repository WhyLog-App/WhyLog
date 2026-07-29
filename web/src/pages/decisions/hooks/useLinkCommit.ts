import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { linkCommit } from "@/apis/applications";
import type { ApiResponse } from "@/types/auth";
import { APPLICATION_CONNECTED_COMMITS_QUERY_KEY } from "./useConnectedCommits";
import { APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY } from "./useRecommendedCommits";

interface UseLinkCommitOptions {
  onSuccess?: () => void;
}

export const useLinkCommit = (
  applicationId: number,
  options?: UseLinkCommitOptions,
) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (commitIds: number[]) =>
      linkCommit(applicationId, { commit_ids: commitIds }),
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
            "커밋 연결에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("커밋 연결에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    linkCommits: (commitIds: number[]) => {
      setErrorMessage(null);
      mutation.mutate(commitIds);
    },
    pendingCommitIds: mutation.isPending ? mutation.variables : null,
    isPending: mutation.isPending,
    errorMessage,
    resetError: () => setErrorMessage(null),
  };
};
