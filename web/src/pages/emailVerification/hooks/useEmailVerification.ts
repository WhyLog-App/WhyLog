import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { issueEmailVerification, verifyEmailVerification } from "@/apis/auth";
import { uploadMemberProfileImage } from "@/apis/members";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse } from "@/types/auth";
import { establishAuthenticatedSession } from "@/utils/authSessionBoundary";
import {
  clearEmailVerificationEmail,
  getEmailVerificationEmail,
  saveEmailVerificationEmail,
} from "@/utils/emailVerificationStorage";
import { validateProfileImageFile } from "@/utils/profileImage";
import {
  clearExpiredSignupProfileImageDrafts,
  clearSignupProfileImageDraft,
  loadSignupProfileImageDraft,
} from "@/utils/signupProfileImageDraftStorage";
import type { EmailVerificationLocationState } from "../types";

const getErrorMessage = (error: unknown, fallback: string) => {
  if (isAxiosError<ApiResponse<unknown>>(error)) {
    return error.response?.data?.message ?? fallback;
  }
  return fallback;
};

export const useEmailVerification = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state ?? {}) as EmailVerificationLocationState;
  const email = state.email?.trim() || getEmailVerificationEmail();
  const routeProfileImage =
    state.profileImage &&
    state.profileImageExpiresAt &&
    state.profileImageExpiresAt > Date.now()
      ? state.profileImage
      : null;
  const [code, setCode] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(
    state.deliveryErrorMessage
      ? `메일 발송 실패: ${state.deliveryErrorMessage} 인증 코드 다시 받기를 눌러주세요.`
      : null,
  );
  const [infoMessage, setInfoMessage] = useState<string | null>(
    state.profileImagePersistenceWarning ??
      (email && !state.deliveryErrorMessage
        ? "메일이 오지 않으면 인증 코드를 다시 받을 수 있습니다."
        : null),
  );
  const [profileImageUploadWarning, setProfileImageUploadWarning] = useState<
    string | null
  >(null);
  const completeVerification = () => {
    clearEmailVerificationEmail();
    navigate(ROUTES.APP_ROOT, { replace: true });
  };

  useEffect(() => {
    if (email) saveEmailVerificationEmail(email);
  }, [email]);

  useEffect(() => {
    void clearExpiredSignupProfileImageDrafts();
  }, []);

  const resendMutation = useMutation({
    mutationFn: () => issueEmailVerification({ email }),
    onSuccess: () => {
      setCode("");
      setErrorMessage(null);
      setInfoMessage("인증 코드를 다시 보냈습니다. 메일함을 확인해주세요.");
    },
    onError: (error: unknown) => {
      setInfoMessage(null);
      setErrorMessage(
        getErrorMessage(error, "인증 코드 재발송에 실패했습니다."),
      );
    },
  });

  const verifyMutation = useMutation({
    mutationFn: () => verifyEmailVerification({ email, code }),
    onSuccess: async (result) => {
      if (!result.access_token) {
        await clearSignupProfileImageDraft();
        setErrorMessage("인증 후 로그인 토큰을 받을 수 없습니다.");
        return;
      }
      establishAuthenticatedSession(result.access_token);

      try {
        const profileImage =
          routeProfileImage ?? (await loadSignupProfileImageDraft(email));

        if (profileImage) {
          const profileImageError = validateProfileImageFile(profileImage);
          if (profileImageError) {
            setProfileImageUploadWarning(
              "이메일 인증은 완료됐지만 프로필 이미지가 올바르지 않아 저장하지 못했습니다. 마이페이지에서 다시 등록해 주세요.",
            );
            return;
          }

          try {
            await uploadMemberProfileImage(profileImage);
          } catch (error) {
            console.error("회원가입 후 프로필 이미지 업로드 실패:", {
              status: isAxiosError(error) ? error.response?.status : undefined,
            });
            setProfileImageUploadWarning(
              "이메일 인증은 완료됐지만 프로필 이미지를 저장하지 못했습니다. 마이페이지에서 다시 등록해 주세요.",
            );
            return;
          }
        }

        completeVerification();
      } finally {
        await clearSignupProfileImageDraft();
      }
    },
    onError: (error: unknown) => {
      setInfoMessage(null);
      setErrorMessage(getErrorMessage(error, "인증 코드 확인에 실패했습니다."));
    },
  });

  const handleCodeChange = (value: string) => {
    setCode(value.replace(/\D/g, "").slice(0, 6));
  };

  const handleVerify = (event: FormEvent) => {
    event.preventDefault();
    if (!email || verifyMutation.isPending || resendMutation.isPending) return;
    setErrorMessage(null);
    setInfoMessage(null);

    if (!/^\d{6}$/.test(code)) {
      setErrorMessage("6자리 숫자 인증 코드를 입력해 주세요.");
      return;
    }

    verifyMutation.mutate();
  };

  const handleResend = () => {
    if (!email || resendMutation.isPending || verifyMutation.isPending) return;
    resendMutation.mutate();
  };

  return {
    email,
    code,
    errorMessage,
    infoMessage,
    isMissingContext: !email,
    isVerifying: verifyMutation.isPending,
    isResending: resendMutation.isPending,
    profileImageUploadWarning,
    setCode: handleCodeChange,
    handleVerify,
    handleResend,
    completeVerification,
  };
};
