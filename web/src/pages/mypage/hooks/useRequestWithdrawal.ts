import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { requestMyWithdrawal } from "@/apis/members";
import { ROUTES } from "@/constants/routes";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import { clearAuthenticatedSession } from "@/utils/authSessionBoundary";
import { mypageErrorMessage } from "../utils/errorMessage";

export const useRequestWithdrawal = () => {
  const navigate = useNavigate();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: requestMyWithdrawal,
    onSuccess: () => {
      clearAuthenticatedSession();
      void clearAuthFlowState();
      navigate(ROUTES.LANDING, { replace: true });
    },
    onError: (error: unknown) => {
      setErrorMessage(mypageErrorMessage(error, "회원 탈퇴에 실패했습니다."));
    },
  });

  return {
    requestWithdrawal: () => {
      setErrorMessage(null);
      mutation.mutate();
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
