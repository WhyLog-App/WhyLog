import { AuthBackground } from "@/components/common/AuthBackground";
import {
  MissingWithdrawalRecoveryContext,
  WithdrawalRecoveryCard,
} from "./components/WithdrawalRecoveryCard";
import { useWithdrawalRecovery } from "./hooks/useWithdrawalRecovery";

function WithdrawalRecoveryPage() {
  const { email, purgeAt, errorMessage, isMissingContext, isPending, recover } =
    useWithdrawalRecovery();

  return (
    <div className="relative flex min-h-dvh w-full items-center justify-center overflow-x-hidden px-4 py-8">
      <AuthBackground />
      {isMissingContext ? (
        <MissingWithdrawalRecoveryContext />
      ) : (
        <WithdrawalRecoveryCard
          email={email}
          purgeAt={purgeAt}
          errorMessage={errorMessage}
          isPending={isPending}
          onRecover={recover}
        />
      )}
    </div>
  );
}

export default WithdrawalRecoveryPage;
