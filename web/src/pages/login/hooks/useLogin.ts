import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import type { FormEvent } from "react";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login, logout } from "@/apis/auth";
import {
  MEMBER_EMAIL_MAX_LENGTH,
  MEMBER_PASSWORD_MAX_LENGTH,
} from "@/constants/member";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse, LoginResult } from "@/types/auth";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import {
  clearAuthenticatedSession,
  establishAuthenticatedSession,
} from "@/utils/authSessionBoundary";
import {
  clearEmailVerificationEmail,
  saveEmailVerificationEmail,
} from "@/utils/emailVerificationStorage";
import { tokenStore } from "@/utils/tokenStore";

export const useLogin = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const loginMutation = useMutation({
    mutationFn: async (credentials: Parameters<typeof login>[0]) => {
      if (tokenStore.hasToken()) {
        await logout();
        clearAuthenticatedSession();
      }
      return login(credentials);
    },
    onSuccess: async (result: LoginResult) => {
      if (result.status === "EMAIL_VERIFICATION_REQUIRED") {
        clearAuthenticatedSession();
        saveEmailVerificationEmail(result.email);
        navigate(ROUTES.EMAIL_VERIFICATION, {
          replace: true,
          state: { email: result.email },
        });
        return;
      }

      if (result.status === "RECOVERY_REQUIRED") {
        clearAuthenticatedSession();
        clearEmailVerificationEmail();
        navigate(ROUTES.WITHDRAWAL_RECOVERY, {
          replace: true,
          state: {
            memberId: result.member_id,
            email: result.email,
            challenge: result.withdrawal_recovery_challenge,
            purgeAt: result.purge_at,
          },
        });
        return;
      }

      if (!result.access_token) {
        setErrorMessage("로그인 토큰을 받을 수 없습니다. 다시 시도해주세요.");
        return;
      }

      establishAuthenticatedSession(result.access_token);
      await clearAuthFlowState();
      const from = location.state?.from?.pathname || ROUTES.APP_ROOT;
      navigate(from, { replace: true });
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "로그인에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("로그인에 실패했습니다. 다시 시도해주세요.");
    },
  });

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    const trimmedEmail = email.trim();
    if (trimmedEmail.length > MEMBER_EMAIL_MAX_LENGTH) {
      setErrorMessage("이메일은 50자 이하로 입력해 주세요.");
      return;
    }
    if (password.length > MEMBER_PASSWORD_MAX_LENGTH) {
      setErrorMessage("비밀번호는 100자 이하로 입력해 주세요.");
      return;
    }

    loginMutation.mutate({ email: trimmedEmail, password });
  };

  return {
    email,
    password,
    errorMessage,
    setEmail,
    setPassword,
    handleSubmit,
    isPending: loginMutation.isPending,
  };
};
