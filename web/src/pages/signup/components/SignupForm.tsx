import { useRef } from "react";
import { Link } from "react-router-dom";
import iconCamera from "@/assets/icons/media/ic_camera.svg";
import { Icon } from "@/components/common/Icon";
import LogoSymbol from "@/components/logo/LogoSymbol";
import LogoText from "@/components/logo/LogoText";
import { ROUTES } from "@/constants/routes";

interface SignupFormProps {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  profileImagePreview: string | null;
  errorMessage: string | null;
  isPending: boolean;
  onNameChange: (value: string) => void;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onConfirmPasswordChange: (value: string) => void;
  onProfileImageChange: (file: File | null) => void;
  onSubmit: (e: React.FormEvent) => void;
}

export const SignupForm = ({
  name,
  email,
  password,
  confirmPassword,
  profileImagePreview,
  errorMessage,
  isPending,
  onNameChange,
  onEmailChange,
  onPasswordChange,
  onConfirmPasswordChange,
  onProfileImageChange,
  onSubmit,
}: SignupFormProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    onProfileImageChange(event.target.files?.[0] ?? null);
  };

  return (
    <form
      onSubmit={onSubmit}
      className="relative flex w-full max-w-110 flex-col items-center gap-6 overflow-hidden rounded-[30px] border border-white px-5 pb-8 pt-10 sm:gap-10 sm:px-7 sm:pb-10 sm:pt-20"
      style={{ background: "rgba(255, 255, 255, 0.3)" }}
    >
      {/* Logo */}
      <section className="flex flex-col items-center gap-3" aria-label="WhyLog">
        <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
        <LogoText className="h-13 w-38" aria-hidden="true" />
        <p className="typo-body5 text-text-secondary">
          회의 기반 의사결정 관리 플랫폼
        </p>
      </section>

      <div className="flex w-full flex-col gap-3">
        <div className="flex w-full flex-col gap-1">
          <p className="typo-label text-text-secondary">프로필 이미지</p>
          <div className="flex w-full items-center gap-3">
            <button
              type="button"
              disabled={isPending}
              className="flex size-12 shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-full border border-white bg-white/10 disabled:cursor-not-allowed disabled:opacity-60"
              onClick={() => fileInputRef.current?.click()}
              aria-label="프로필 이미지 업로드"
            >
              {profileImagePreview ? (
                <img
                  src={profileImagePreview}
                  alt="프로필 이미지 미리보기"
                  className="size-full object-cover"
                />
              ) : (
                <Icon
                  icon={iconCamera}
                  size={16}
                  className="text-text-inverse"
                />
              )}
            </button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={handleFileChange}
              aria-label="프로필 이미지 파일 선택"
            />
            <p className="typo-caption1 text-text-tertiary">
              클릭하여 프로필 사진을 업로드하세요
            </p>
          </div>
        </div>

        <div className="flex flex-col gap-1">
          <label htmlFor="name" className="typo-label text-text-secondary">
            이름
          </label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => onNameChange(e.target.value)}
            placeholder="Name"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label htmlFor="email" className="typo-label text-text-secondary">
            이메일
          </label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => onEmailChange(e.target.value)}
            placeholder="E-mail"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label htmlFor="password" className="typo-label text-text-secondary">
            비밀번호
          </label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            placeholder="Password"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label
            htmlFor="confirm-password"
            className="typo-label text-text-secondary"
          >
            비밀번호 확인
          </label>
          <input
            id="confirm-password"
            type="password"
            value={confirmPassword}
            onChange={(e) => onConfirmPasswordChange(e.target.value)}
            placeholder="Confirm Password"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          />
        </div>
      </div>

      <div className="flex w-full flex-col items-center gap-3">
        {errorMessage && (
          <p className="typo-body6 text-red-500" role="alert">
            {errorMessage}
          </p>
        )}
        <button
          type="submit"
          disabled={isPending}
          className="typo-button-md flex w-full cursor-pointer items-center justify-center rounded-full py-3 text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          style={{
            background:
              "linear-gradient(164.49deg, rgb(91, 141, 239) 15.47%, rgb(0, 99, 247) 84.42%)",
          }}
        >
          {isPending ? "가입 중..." : "가입하기"}
        </button>

        <div className="flex items-center justify-center gap-2 whitespace-nowrap">
          <span className="typo-body6 text-text-secondary">
            이미 계정이 있으신가요?
          </span>
          <Link
            to={ROUTES.LOGIN}
            className="typo-button-sm text-text-brand hover:underline"
          >
            로그인
          </Link>
        </div>
      </div>
    </form>
  );
};
