import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { verifyWithdrawalRecovery } from "@/apis/auth";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse } from "@/types/auth";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import { establishAuthenticatedSession } from "@/utils/authSessionBoundary";
import type { WithdrawalRecoveryLocationState } from "../types";

export const useWithdrawalRecovery = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as WithdrawalRecoveryLocationState;
  const memberId = state.memberId;
  const email = state.email ?? "";
  const challenge = state.challenge ?? "";
  const purgeAt = state.purgeAt ?? null;
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const mutation = useMutation({
    mutationFn: () => {
      if (memberId == null || !challenge) {
        throw new Error("복구 정보가 없습니다.");
      }
      return verifyWithdrawalRecovery({ member_id: memberId, challenge });
    },
    onSuccess: async (result) => {
      if (!result.access_token) {
        setErrorMessage("복구 후 로그인 토큰을 받을 수 없습니다.");
        return;
      }
      establishAuthenticatedSession(result.access_token);
      await clearAuthFlowState();
      navigate(ROUTES.APP_ROOT, { replace: true });
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ?? "계정 복구에 실패했습니다.",
        );
        return;
      }
      setErrorMessage("계정 복구에 실패했습니다.");
    },
  });

  return {
    email,
    purgeAt,
    errorMessage,
    isMissingContext: memberId == null || !challenge,
    isPending: mutation.isPending,
    recover: () => {
      setErrorMessage(null);
      mutation.mutate();
    },
  };
};
