import { Link } from "react-router-dom";
import LogoSymbol from "@/components/logo/LogoSymbol";
import LogoText from "@/components/logo/LogoText";
import { ROUTES } from "@/constants/routes";

interface EmailVerificationFormProps {
  email: string;
  code: string;
  errorMessage: string | null;
  infoMessage: string | null;
  isVerifying: boolean;
  isResending: boolean;
  onCodeChange: (value: string) => void;
  onVerify: (event: React.FormEvent) => void;
  onResend: () => void;
}

export const EmailVerificationForm = ({
  email,
  code,
  errorMessage,
  infoMessage,
  isVerifying,
  isResending,
  onCodeChange,
  onVerify,
  onResend,
}: EmailVerificationFormProps) => {
  return (
    <form
      onSubmit={onVerify}
      className="relative flex w-full max-w-110 flex-col items-center gap-8 overflow-hidden rounded-[30px] border border-white bg-white/30 px-5 py-10 sm:px-7 sm:py-12"
    >
      <section className="flex flex-col items-center gap-3" aria-label="WhyLog">
        <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
        <LogoText className="h-13 w-38" aria-hidden="true" />
        <p className="typo-body5 text-center text-text-secondary">
          {email}로 보낸 인증 코드를 입력하세요.
        </p>
      </section>

      <div className="flex w-full flex-col gap-2">
        <label htmlFor="email-code" className="typo-label text-text-secondary">
          이메일 인증 코드
        </label>
        <input
          id="email-code"
          type="text"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={6}
          value={code}
          onChange={(e) => onCodeChange(e.target.value)}
          placeholder="000000"
          className="typo-h5 h-12 w-full rounded-full border border-white bg-transparent px-4 py-3 text-center tracking-[0.4em] text-text-primary placeholder:text-text-tertiary focus:outline-none focus:ring-1 focus:ring-white"
          aria-describedby="email-code-help"
        />
        <p id="email-code-help" className="typo-caption1 text-text-tertiary">
          6자리 숫자만 입력할 수 있습니다.
        </p>
      </div>

      <div className="flex w-full flex-col items-center gap-3">
        {errorMessage && (
          <p className="typo-body6 text-red-500" role="alert">
            {errorMessage}
          </p>
        )}
        {infoMessage && (
          <p className="typo-body6 text-text-secondary" role="status">
            {infoMessage}
          </p>
        )}
        <button
          type="submit"
          disabled={isVerifying || isResending}
          className="typo-button-md w-full cursor-pointer rounded-full bg-(--color-action-primary) py-3 text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isVerifying ? "확인 중..." : "인증 완료"}
        </button>
        <button
          type="button"
          onClick={onResend}
          disabled={isVerifying || isResending}
          className="typo-button-md cursor-pointer text-text-brand hover:underline disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isResending ? "재발송 중..." : "인증 코드 다시 받기"}
        </button>
      </div>
    </form>
  );
};

export const MissingEmailVerificationContext = () => {
  return (
    <div className="relative flex w-full max-w-110 flex-col items-center gap-6 overflow-hidden rounded-[30px] border border-white bg-white/30 px-5 py-10 text-center sm:px-7 sm:py-12">
      <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
      <div className="flex flex-col gap-2">
        <h1 className="typo-h5 text-text-primary">
          인증할 이메일 정보가 없습니다.
        </h1>
        <p className="typo-body5 text-text-secondary">
          회원가입 또는 미인증 계정 로그인 후 열린 인증 화면에서 코드를 확인할
          수 있습니다.
        </p>
      </div>
      <div className="grid w-full grid-cols-2 items-center gap-6 text-center">
        <Link
          to={ROUTES.SIGNUP}
          className="typo-button-md text-text-brand hover:underline"
        >
          회원가입으로 이동
        </Link>
        <Link
          to={ROUTES.LOGIN}
          className="typo-button-md text-text-brand hover:underline"
        >
          로그인으로 이동
        </Link>
      </div>
    </div>
  );
};
