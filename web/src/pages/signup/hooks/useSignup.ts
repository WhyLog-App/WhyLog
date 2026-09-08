import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { logout, signup } from "@/apis/auth";
import {
  MEMBER_EMAIL_MAX_LENGTH,
  MEMBER_NAME_MAX_LENGTH,
  MEMBER_PASSWORD_MAX_LENGTH,
} from "@/constants/member";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse } from "@/types/auth";
import { clearAuthenticatedSession } from "@/utils/authSessionBoundary";
import { saveEmailVerificationEmail } from "@/utils/emailVerificationStorage";
import { validateProfileImageFile } from "@/utils/profileImage";
import {
  clearExpiredSignupProfileImageDrafts,
  clearSignupProfileImageDraft,
  saveSignupProfileImageDraft,
} from "@/utils/signupProfileImageDraftStorage";
import { tokenStore } from "@/utils/tokenStore";

const EMAIL_VERIFICATION_DELIVERY_FAILED_CODE = "AUTH502_1";

export const useSignup = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [profileImage, setProfileImage] = useState<File | null>(null);
  const [profileImagePreview, setProfileImagePreview] = useState<string | null>(
    null,
  );
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  useEffect(() => {
    void clearExpiredSignupProfileImageDrafts();
  }, []);

  useEffect(() => {
    return () => {
      if (profileImagePreview) URL.revokeObjectURL(profileImagePreview);
    };
  }, [profileImagePreview]);

  const navigateToEmailVerification = async (
    verificationEmail: string,
    deliveryErrorMessage?: string,
  ) => {
    const trimmedEmail = verificationEmail.trim();
    clearAuthenticatedSession();
    saveEmailVerificationEmail(trimmedEmail);

    const canPersistProfileImage =
      profileImage && !validateProfileImageFile(profileImage);
    const persistenceResult = canPersistProfileImage
      ? await saveSignupProfileImageDraft(trimmedEmail, profileImage)
      : null;

    if (!canPersistProfileImage) {
      await clearSignupProfileImageDraft();
    }

    navigate(ROUTES.EMAIL_VERIFICATION, {
      replace: true,
      state: {
        email: trimmedEmail,
        profileImage: persistenceResult?.warning ? profileImage : null,
        profileImageExpiresAt: persistenceResult?.expiresAt ?? null,
        deliveryErrorMessage,
        profileImagePersistenceWarning: persistenceResult?.warning ?? null,
      },
    });
  };

  const signupMutation = useMutation({
    mutationFn: async () => {
      if (tokenStore.hasToken()) {
        await logout();
        clearAuthenticatedSession();
      }
      return signup({
        name: name.trim(),
        email: email.trim(),
        password,
      });
    },
    onSuccess: async (result) => {
      await navigateToEmailVerification(result.email);
    },
    onError: async (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        const response = error.response?.data;
        const message =
          response?.message ?? "회원가입에 실패했습니다. 다시 시도해주세요.";

        if (response?.code === EMAIL_VERIFICATION_DELIVERY_FAILED_CODE) {
          await navigateToEmailVerification(email, message);
          return;
        }

        setErrorMessage(message);
        return;
      }
      setErrorMessage("회원가입에 실패했습니다. 다시 시도해주세요.");
    },
  });

  const validate = () => {
    if (!name.trim()) return "이름을 입력해 주세요.";
    if (name.trim().length > MEMBER_NAME_MAX_LENGTH) {
      return "이름은 50자 이하로 입력해 주세요.";
    }
    if (!email.trim()) return "이메일을 입력해 주세요.";
    if (email.trim().length > MEMBER_EMAIL_MAX_LENGTH) {
      return "이메일은 50자 이하로 입력해 주세요.";
    }
    if (!password.trim()) return "비밀번호를 입력해 주세요.";
    if (password.length < 8) return "비밀번호는 8자 이상이어야 합니다.";
    if (password.length > MEMBER_PASSWORD_MAX_LENGTH) {
      return "비밀번호는 100자 이하로 입력해 주세요.";
    }
    if (password !== confirmPassword) return "비밀번호가 일치하지 않습니다.";
    return null;
  };

  const handleProfileImageChange = (file: File | null) => {
    if (profileImagePreview) {
      URL.revokeObjectURL(profileImagePreview);
    }

    if (!file) {
      setProfileImage(null);
      setProfileImagePreview(null);
      return;
    }

    const profileImageError = validateProfileImageFile(file);
    if (profileImageError) {
      setProfileImage(null);
      setProfileImagePreview(null);
      setErrorMessage(profileImageError);
      return;
    }

    setErrorMessage(null);
    setProfileImage(file);
    setProfileImagePreview(URL.createObjectURL(file));
  };

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (signupMutation.isPending) return;
    setErrorMessage(null);

    const validationError = validate();
    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    signupMutation.mutate();
  };

  return {
    name,
    email,
    password,
    confirmPassword,
    profileImagePreview,
    errorMessage,
    setName,
    setEmail,
    setPassword,
    setConfirmPassword,
    handleProfileImageChange,
    handleSubmit,
    isPending: signupMutation.isPending,
  };
};
