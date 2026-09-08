import { useState } from "react";
import Modal from "@/components/common/Modal";
import { ValidationMessage } from "@/components/common/ValidationMessage";
import { useRequestWithdrawal } from "../hooks/useRequestWithdrawal";

export const WithdrawalSection = () => {
  const [isWithdrawalOpen, setIsWithdrawalOpen] = useState(false);
  const withdrawalMutation = useRequestWithdrawal();

  return (
    <>
      <section className="flex flex-col items-start gap-3 pt-8 text-left">
        <div>
          <h2 className="typo-h5 text-(--color-text-primary)">회원 탈퇴</h2>
          <p className="typo-body6 text-(--color-text-tertiary)">
            탈퇴 후 30일 동안 복구할 수 있고, 이후 계정은 복구할 수 없습니다.
          </p>
        </div>
        <button
          type="button"
          onClick={() => setIsWithdrawalOpen(true)}
          className="typo-button-md min-h-11 w-fit cursor-pointer bg-transparent py-1 text-red-600 underline-offset-4 hover:underline"
        >
          회원 탈퇴
        </button>
      </section>

      {isWithdrawalOpen && (
        <Modal
          title="회원 탈퇴"
          onClose={() =>
            !withdrawalMutation.isPending && setIsWithdrawalOpen(false)
          }
          primaryLabel={
            withdrawalMutation.isPending ? "처리 중..." : "탈퇴 요청"
          }
          onPrimaryClick={withdrawalMutation.requestWithdrawal}
          isPrimaryDisabled={withdrawalMutation.isPending}
        >
          <div className="flex flex-col gap-2">
            <p className="typo-body4 text-(--color-text-secondary)">
              탈퇴하면 서비스 진입이 차단됩니다. 30일 안에 로그인하면 계정을
              복구할 수 있습니다.
            </p>
            <ValidationMessage message={withdrawalMutation.errorMessage} />
          </div>
        </Modal>
      )}
    </>
  );
};
