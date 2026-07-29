import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { login } from "@/apis/auth";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse, LoginResult } from "@/types/auth";
import { tokenStore } from "@/utils/tokenStore";

export const useLogin = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: (result: LoginResult) => {
      tokenStore.setToken(result.access_token);
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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    loginMutation.mutate({ email, password });
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
