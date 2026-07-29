import { useMutation } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { signup } from "@/apis/auth";
import { ROUTES } from "@/constants/routes";
import type { ApiResponse } from "@/types/auth";
import { tokenStore } from "@/utils/tokenStore";
import { useUploadMemberProfileImage } from "./useUploadMemberProfileImage";

export const useSignup = () => {
  const navigate = useNavigate();
  const uploadProfileImageMutation = useUploadMemberProfileImage();
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
    return () => {
      if (profileImagePreview) URL.revokeObjectURL(profileImagePreview);
    };
  }, [profileImagePreview]);

  const signupMutation = useMutation({
    mutationFn: async () => {
      const result = await signup({
        name: name.trim(),
        email: email.trim(),
        password,
      });

      tokenStore.setToken(result.access_token);

      if (profileImage) {
        try {
          await uploadProfileImageMutation.mutateAsync(profileImage);
        } catch (error) {
          console.error("프로필 이미지 업로드 실패:", error);
        }
      }

      return result;
    },
    onSuccess: () => {
      navigate(ROUTES.APP_ROOT, { replace: true });
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "회원가입에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("회원가입에 실패했습니다. 다시 시도해주세요.");
    },
  });

  const validate = () => {
    if (!name.trim()) return "이름을 입력해 주세요.";
    if (!email.trim()) return "이메일을 입력해 주세요.";
    if (!password.trim()) return "비밀번호를 입력해 주세요.";
    if (password.length < 8) return "비밀번호는 8자 이상이어야 합니다.";
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

    if (!file.type.startsWith("image/")) {
      setProfileImage(null);
      setProfileImagePreview(null);
      setErrorMessage("이미지 파일만 업로드할 수 있습니다.");
      return;
    }

    setErrorMessage(null);
    setProfileImage(file);
    setProfileImagePreview(URL.createObjectURL(file));
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (signupMutation.isPending || uploadProfileImageMutation.isPending)
      return;
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
    isPending: signupMutation.isPending || uploadProfileImageMutation.isPending,
  };
};
