import type { FormEvent } from "react";
import { useState } from "react";
import GlassCard from "@/components/common/GlassCard";
import Modal from "@/components/common/Modal";
import { PasswordInput } from "@/components/common/PasswordInput";
import { ValidationMessage } from "@/components/common/ValidationMessage";
import { MEMBER_PASSWORD_MAX_LENGTH } from "@/constants/member";
import { usePasswordMutations } from "../hooks/usePasswordMutations";

interface PasswordChangeSectionProps {
  inputClassName: string;
}

export const PasswordChangeSection = ({
  inputClassName,
}: PasswordChangeSectionProps) => {
  const [currentPassword, setCurrentPassword] = useState("");
  const [isNewPasswordStep, setIsNewPasswordStep] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [newPasswordConfirm, setNewPasswordConfirm] = useState("");
  const [passwordValidation, setPasswordValidation] = useState<string | null>(
    null,
  );
  const passwordMutations = usePasswordMutations();

  const handleStartPasswordChange = async () => {
    if (isNewPasswordStep || !currentPassword || passwordMutations.isVerifying)
      return;
    setPasswordValidation(null);
    passwordMutations.resetErrors();
    const isVerified =
      await passwordMutations.verifyCurrentPassword(currentPassword);
    if (isVerified) {
      setIsNewPasswordStep(true);
    }
  };

  const handlePasswordSubmit = (event: FormEvent) => {
    event.preventDefault();
    if (!isNewPasswordStep || passwordMutations.isChanging) return;
    setPasswordValidation(null);
    if (newPassword.length < 8) {
      setPasswordValidation("새 비밀번호는 8자 이상이어야 합니다.");
      return;
    }
    if (newPassword.length > MEMBER_PASSWORD_MAX_LENGTH) {
      setPasswordValidation("새 비밀번호는 100자 이하로 입력해 주세요.");
      return;
    }
    if (newPassword !== newPasswordConfirm) {
      setPasswordValidation("새 비밀번호가 일치하지 않습니다.");
      return;
    }
    passwordMutations.changePassword(currentPassword, newPassword);
  };

  const isFinalPasswordReady =
    isNewPasswordStep &&
    newPassword.length >= 8 &&
    newPassword.length <= MEMBER_PASSWORD_MAX_LENGTH &&
    newPassword === newPasswordConfirm &&
    !passwordMutations.isChanging;
  const livePasswordValidation = !isNewPasswordStep
    ? null
    : newPassword.length > MEMBER_PASSWORD_MAX_LENGTH
      ? "새 비밀번호는 100자 이하로 입력해 주세요."
      : newPassword.length > 0 && newPassword.length < 8
        ? "새 비밀번호는 8자 이상이어야 합니다."
        : newPasswordConfirm.length > 0 && newPassword !== newPasswordConfirm
          ? "새 비밀번호가 일치하지 않습니다."
          : null;

  return (
    <>
      <section className="grid min-w-0 grid-rows-[auto_1fr] gap-4">
        <div className="lg:min-h-14">
          <h2 className="typo-h5 text-(--color-text-primary)">비밀번호 변경</h2>
          <p className="typo-body6 text-(--color-text-tertiary)">
            현재 비밀번호를 확인한 뒤 새 비밀번호를 입력합니다.
          </p>
        </div>
        <GlassCard className="h-full min-h-73 p-5 sm:p-6">
          <form
            onSubmit={handlePasswordSubmit}
            className={`grid h-full gap-3 sm:grid-cols-[minmax(0,1fr)_auto] sm:gap-x-6 ${
              isNewPasswordStep ? "content-start" : "content-center sm:pb-20"
            }`}
          >
            <div className="min-w-0 sm:col-start-1 sm:row-start-1">
              <PasswordInput
                id="current-password"
                label="현재 비밀번호"
                value={currentPassword}
                onChange={setCurrentPassword}
                placeholder="현재 비밀번호"
                autoComplete="current-password"
                maxLength={MEMBER_PASSWORD_MAX_LENGTH}
                inputClassName={inputClassName}
                disabled={isNewPasswordStep || passwordMutations.isVerifying}
              />
            </div>
            <button
              type="button"
              onClick={handleStartPasswordChange}
              disabled={
                isNewPasswordStep ||
                !currentPassword ||
                passwordMutations.isVerifying
              }
              className="typo-button-md h-11 w-full shrink-0 cursor-pointer rounded-xl bg-(--color-action-primary) px-5 text-white disabled:cursor-not-allowed disabled:opacity-60 sm:col-start-2 sm:row-start-1 sm:w-fit sm:self-end"
            >
              {passwordMutations.isVerifying ? "확인 중..." : "확인하기"}
            </button>

            {passwordMutations.verifyErrorMessage && (
              <div className="sm:col-start-1 sm:row-start-2">
                <ValidationMessage
                  message={passwordMutations.verifyErrorMessage}
                />
              </div>
            )}

            {isNewPasswordStep && (
              <>
                <div className="grid min-w-0 gap-3 sm:col-start-1 sm:row-start-3">
                  <PasswordInput
                    id="new-password"
                    label="새 비밀번호"
                    value={newPassword}
                    onChange={(value) => {
                      setNewPassword(value);
                      setPasswordValidation(null);
                    }}
                    placeholder="새 비밀번호"
                    autoComplete="new-password"
                    maxLength={MEMBER_PASSWORD_MAX_LENGTH}
                    inputClassName={inputClassName}
                  />
                  <PasswordInput
                    id="new-password-confirm"
                    label="새 비밀번호 확인"
                    value={newPasswordConfirm}
                    onChange={(value) => {
                      setNewPasswordConfirm(value);
                      setPasswordValidation(null);
                    }}
                    placeholder="새 비밀번호 확인"
                    autoComplete="new-password"
                    maxLength={MEMBER_PASSWORD_MAX_LENGTH}
                    inputClassName={inputClassName}
                  />
                  <ValidationMessage
                    message={
                      passwordValidation ??
                      livePasswordValidation ??
                      passwordMutations.changeErrorMessage
                    }
                  />
                </div>
                <button
                  type="submit"
                  disabled={!isFinalPasswordReady}
                  className="typo-button-md w-full cursor-pointer rounded-xl bg-(--color-action-primary) px-5 py-2.5 text-white disabled:cursor-not-allowed disabled:opacity-60 sm:col-start-2 sm:row-start-3 sm:w-fit sm:self-end"
                >
                  {passwordMutations.isChanging ? "변경 중..." : "변경하기"}
                </button>
              </>
            )}
          </form>
        </GlassCard>
      </section>

      {passwordMutations.isPasswordChanged && (
        <Modal
          title="비밀번호가 변경되었습니다"
          onClose={passwordMutations.completePasswordChange}
          primaryLabel="확인"
          onPrimaryClick={passwordMutations.completePasswordChange}
          showCancel={false}
          isDismissible={false}
        >
          <p className="typo-body4 text-(--color-text-secondary)">
            다시 로그인해 주세요.
          </p>
        </Modal>
      )}
    </>
  );
};
