import { Link } from "react-router-dom";
import LogoSymbol from "@/components/logo/LogoSymbol";
import LogoText from "@/components/logo/LogoText";
import { ROUTES } from "@/constants/routes";
import { formatDateTime } from "@/utils/date";

interface WithdrawalRecoveryCardProps {
  email: string;
  purgeAt: string | null;
  errorMessage: string | null;
  isPending: boolean;
  onRecover: () => void;
}

export const WithdrawalRecoveryCard = ({
  email,
  purgeAt,
  errorMessage,
  isPending,
  onRecover,
}: WithdrawalRecoveryCardProps) => {
  return (
    <div className="relative flex w-full max-w-110 flex-col items-center gap-8 overflow-hidden rounded-[30px] border border-white bg-white/30 px-5 py-10 text-center sm:px-7 sm:py-12">
      <section className="flex flex-col items-center gap-3" aria-label="WhyLog">
        <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
        <LogoText className="h-13 w-38" aria-hidden="true" />
      </section>
      <div className="flex flex-col gap-2">
        <h1 className="typo-h5 text-text-primary">
          탈퇴 유예 중인 계정입니다.
        </h1>
        <p className="typo-body5 text-text-secondary">
          {email} 계정은 아직 복구할 수 있습니다.
        </p>
        <p className="typo-body6 text-text-tertiary">
          복구 가능 기한: {formatDateTime(purgeAt)}
        </p>
      </div>
      {errorMessage && (
        <p className="typo-body6 text-red-500" role="alert">
          {errorMessage}
        </p>
      )}
      <button
        type="button"
        onClick={onRecover}
        disabled={isPending}
        className="typo-button-md w-full cursor-pointer rounded-full bg-(--color-action-primary) py-3 text-white transition-opacity hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {isPending ? "복구 중..." : "계정 복구하기"}
      </button>
      <Link
        to={ROUTES.LOGIN}
        replace
        state={null}
        className="typo-button-md text-text-brand hover:underline"
      >
        다른 계정으로 로그인
      </Link>
    </div>
  );
};

export const MissingWithdrawalRecoveryContext = () => {
  return (
    <div className="relative flex w-full max-w-110 flex-col items-center gap-6 overflow-hidden rounded-[30px] border border-white bg-white/30 px-5 py-10 text-center sm:px-7 sm:py-12">
      <LogoSymbol className="h-15 w-17.5" aria-hidden="true" />
      <div className="flex flex-col gap-2">
        <h1 className="typo-h5 text-text-primary">
          복구할 계정 정보가 없습니다.
        </h1>
        <p className="typo-body5 text-text-secondary">
          탈퇴 유예 계정으로 로그인하면 복구 화면으로 이동합니다.
        </p>
      </div>
      <Link
        to={ROUTES.LOGIN}
        replace
        state={null}
        className="typo-button-md text-text-brand hover:underline"
      >
        로그인으로 이동
      </Link>
    </div>
  );
};
