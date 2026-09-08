import { AuthBackground } from "@/components/common/AuthBackground";
import Modal from "@/components/common/Modal";
import {
  EmailVerificationForm,
  MissingEmailVerificationContext,
} from "./components/EmailVerificationForm";
import { useEmailVerification } from "./hooks/useEmailVerification";

function EmailVerificationPage() {
  const {
    email,
    code,
    errorMessage,
    infoMessage,
    isMissingContext,
    isVerifying,
    isResending,
    profileImageUploadWarning,
    setCode,
    handleVerify,
    handleResend,
    completeVerification,
  } = useEmailVerification();

  return (
    <div className="relative flex min-h-dvh w-full items-center justify-center overflow-x-hidden px-4 py-8">
      <AuthBackground />
      {isMissingContext ? (
        <MissingEmailVerificationContext />
      ) : (
        <EmailVerificationForm
          email={email}
          code={code}
          errorMessage={errorMessage}
          infoMessage={infoMessage}
          isVerifying={isVerifying}
          isResending={isResending}
          onCodeChange={setCode}
          onVerify={handleVerify}
          onResend={handleResend}
        />
      )}
      {profileImageUploadWarning && (
        <Modal
          title="프로필 이미지 저장 실패"
          onClose={completeVerification}
          primaryLabel="계속하기"
          onPrimaryClick={completeVerification}
          showCancel={false}
          isDismissible={false}
        >
          <p className="typo-body4 text-(--color-text-secondary)">
            {profileImageUploadWarning}
          </p>
        </Modal>
      )}
    </div>
  );
}

export default EmailVerificationPage;
