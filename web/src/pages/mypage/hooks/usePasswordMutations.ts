import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { changeMyPassword, verifyMyPassword } from "@/apis/members";
import { ROUTES } from "@/constants/routes";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import { clearAuthenticatedSession } from "@/utils/authSessionBoundary";
import {
  logMypageRequestFailure,
  mypageErrorMessage,
} from "../utils/errorMessage";

export const usePasswordMutations = () => {
  const navigate = useNavigate();
  const [verifyErrorMessage, setVerifyErrorMessage] = useState<string | null>(
    null,
  );
  const [changeErrorMessage, setChangeErrorMessage] = useState<string | null>(
    null,
  );
  const [isPasswordChanged, setIsPasswordChanged] = useState(false);

  const verifyMutation = useMutation({
    mutationFn: verifyMyPassword,
    onSuccess: () => {
      setVerifyErrorMessage(null);
    },
    onError: (error: unknown) => {
      setVerifyErrorMessage(
        mypageErrorMessage(error, "현재 비밀번호 확인에 실패했습니다."),
      );
    },
  });

  const changeMutation = useMutation({
    mutationFn: changeMyPassword,
    onSuccess: () => {
      setChangeErrorMessage(null);
      setIsPasswordChanged(true);
    },
    onError: (error: unknown) => {
      setChangeErrorMessage(
        mypageErrorMessage(error, "비밀번호 변경에 실패했습니다."),
      );
    },
  });

  return {
    verifyCurrentPassword: async (currentPassword: string) => {
      setVerifyErrorMessage(null);
      try {
        await verifyMutation.mutateAsync({ current_password: currentPassword });
        return true;
      } catch (error) {
        logMypageRequestFailure("현재 비밀번호 확인 요청 실패", error);
        return false;
      }
    },
    changePassword: (currentPassword: string, newPassword: string) => {
      setChangeErrorMessage(null);
      changeMutation.mutate({
        current_password: currentPassword,
        new_password: newPassword,
      });
    },
    completePasswordChange: () => {
      clearAuthenticatedSession();
      void clearAuthFlowState();
      navigate(ROUTES.LOGIN, { replace: true });
    },
    resetErrors: () => {
      setVerifyErrorMessage(null);
      setChangeErrorMessage(null);
    },
    isVerifying: verifyMutation.isPending,
    isChanging: changeMutation.isPending,
    verifyErrorMessage,
    changeErrorMessage,
    isPasswordChanged,
  };
};
