import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { inviteTeamMember } from "@/apis/teams";
import type { ApiResponse } from "@/types/auth";
import type { InviteTeamMemberResult } from "@/types/team";

interface UseInviteTeamMemberOptions {
  onSuccess?: (result: InviteTeamMemberResult) => void;
}

export const useInviteTeamMember = (
  teamId: number | null | undefined,
  options?: UseInviteTeamMemberOptions,
) => {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (memberEmail: string) => {
      if (teamId == null) {
        return Promise.reject(new Error("팀 정보를 확인할 수 없습니다."));
      }
      return inviteTeamMember(teamId, { member_email: memberEmail });
    },
    onSuccess: (result) => {
      options?.onSuccess?.(result);
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "팀원 초대에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("팀원 초대에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    invite: (email: string) => {
      setErrorMessage(null);
      mutation.mutate(email);
    },
    isPending: mutation.isPending,
    errorMessage,
    resetError: () => setErrorMessage(null),
  };
};
