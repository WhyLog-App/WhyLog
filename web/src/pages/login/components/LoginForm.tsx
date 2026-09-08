import { Link } from "react-router-dom";
import { PasswordInput } from "@/components/common/PasswordInput";
import { ValidationMessage } from "@/components/common/ValidationMessage";
import LogoSymbol from "@/components/logo/LogoSymbol";
import LogoText from "@/components/logo/LogoText";
import {
  MEMBER_EMAIL_MAX_LENGTH,
  MEMBER_PASSWORD_MAX_LENGTH,
} from "@/constants/member";
import { ROUTES } from "@/constants/routes";

interface LoginFormProps {
  email: string;
  password: string;
  errorMessage: string | null;
  isPending: boolean;
  onEmailChange: (value: string) => void;
  onPasswordChange: (value: string) => void;
  onSubmit: (e: React.FormEvent) => void;
}

export const LoginForm = ({
  email,
  password,
  errorMessage,
  isPending,
  onEmailChange,
  onPasswordChange,
  onSubmit,
}: LoginFormProps) => {
  return (
    <form
      onSubmit={onSubmit}
      className="relative flex w-full max-w-110 flex-col items-center gap-8 overflow-hidden rounded-[30px] border border-white px-5 pb-8 pt-12 sm:gap-10 sm:px-7 sm:pb-10 sm:pt-20"
      style={{ background: "rgba(255, 255, 255, 0.3)" }}
    >
      {/* Logo */}
      <section className="flex flex-col items-center gap-3" aria-label="WhyLog">
        <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
        <LogoText className="h-13 w-38" aria-hidden="true" />

        {/* Subtitle */}
        <p className="typo-body5 text-text-secondary">
          회의 기반 의사결정 관리 플랫폼
        </p>
      </section>

      {/* Form Fields */}
      <div className="flex w-full flex-col gap-3">
        {/* Email Input */}
        <div className="flex flex-col gap-1">
          <label htmlFor="email" className="typo-label text-text-secondary">
            이메일
          </label>
          <input
            id="email"
            type="email"
            value={email}
            maxLength={MEMBER_EMAIL_MAX_LENGTH}
            onChange={(e) => onEmailChange(e.target.value)}
            placeholder="E-mail"
            className="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          />
        </div>

        {/* Password Input */}
        <PasswordInput
          id="password"
          label="비밀번호"
          value={password}
          onChange={onPasswordChange}
          placeholder="Password"
          autoComplete="current-password"
          maxLength={MEMBER_PASSWORD_MAX_LENGTH}
          labelClassName="typo-label text-text-secondary"
          inputClassName="typo-body6 h-11 w-full rounded-full border border-white bg-transparent px-4 py-3 text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
        />
      </div>

      {/* Footer */}
      <div className="flex w-full flex-col items-center gap-3">
        <ValidationMessage message={errorMessage} />
        <button
          type="submit"
          disabled={isPending}
          className="typo-button-md w-full cursor-pointer rounded-full py-3 text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
          style={{
            background:
              "linear-gradient(164.49deg, rgb(91, 141, 239) 15.47%, rgb(0, 99, 247) 84.42%)",
          }}
        >
          {isPending ? "로그인 중..." : "로그인"}
        </button>

        <div className="flex items-center gap-2">
          <span className="typo-body6 text-text-secondary">
            계정이 없으신가요?
          </span>
          <Link
            to={ROUTES.SIGNUP}
            className="typo-button-sm text-text-brand hover:underline"
          >
            회원가입
          </Link>
        </div>
      </div>
    </form>
  );
};
