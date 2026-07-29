import { useState } from "react";
import Modal from "@/components/common/Modal";

interface GitTokenModalProps {
  onClose: () => void;
  onRegister: (accessToken: string) => Promise<boolean>;
  onNext?: () => void;
  isPending?: boolean;
  errorMessage?: string | null;
}

const GitTokenModal = ({
  onClose,
  onRegister,
  onNext,
  isPending = false,
  errorMessage,
}: GitTokenModalProps) => {
  const [token, setToken] = useState("");
  const [validationErrorMessage, setValidationErrorMessage] = useState("");
  const displayErrorMessage = validationErrorMessage || errorMessage;

  const handleTokenSubmit = async () => {
    if (isPending) return;

    const trimmedToken = token.trim();

    if (!trimmedToken) {
      setValidationErrorMessage("토큰을 입력해주세요");
      return;
    }

    setValidationErrorMessage("");
    try {
      const isSuccess = await onRegister(trimmedToken);
      if (isSuccess) onNext?.();
    } catch {
      // 상위에서 전달한 errorMessage 렌더링 경로를 사용
    }
  };

  const handleClose = () => {
    setToken("");
    setValidationErrorMessage("");
    onClose();
  };

  return (
    <Modal
      title="GitHub 토큰 등록"
      onClose={handleClose}
      primaryLabel={isPending ? "등록 중..." : "다음"}
      onPrimaryClick={handleTokenSubmit}
      isPrimaryDisabled={isPending || !token.trim()}
    >
      <div className="flex flex-col gap-1">
        <label
          htmlFor="git-token"
          className="typo-label text-(--color-text-secondary)"
        >
          토큰
        </label>
        <input
          id="git-token"
          type="password"
          value={token}
          onChange={(e) => {
            setToken(e.target.value);
            if (validationErrorMessage) setValidationErrorMessage("");
          }}
          placeholder="토큰을 입력하세요"
          className="typo-body6 h-11 w-full rounded-full border border-white bg-white/50 px-4 py-3 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none"
          disabled={isPending}
        />

        {displayErrorMessage && (
          <div className="pt-1 typo-caption1 text-red-500">
            {displayErrorMessage}
          </div>
        )}
      </div>
    </Modal>
  );
};

export default GitTokenModal;
