import { useState } from "react";
import Modal from "@/components/common/Modal";
import type { AddRepositoryRequest } from "@/types/git";

interface Props {
  onClose: () => void;
  onAdd: (payload: AddRepositoryRequest) => void;
  isPending?: boolean;
  errorMessage?: string | null;
}

const RepositoryAddModal = ({
  onClose,
  onAdd,
  isPending = false,
  errorMessage = null,
}: Props) => {
  const [name, setName] = useState("");
  const [url, setUrl] = useState("");
  const [validationErrorMessage, setValidationErrorMessage] = useState("");
  const displayErrorMessage = validationErrorMessage || errorMessage;

  const handlePrimary = () => {
    if (isPending) return;

    const trimmedName = name.trim();
    const trimmedUrl = url.trim();

    if (!trimmedName || !trimmedUrl) {
      setValidationErrorMessage("이름과 URL을 모두 입력해주세요");
      return;
    }

    setValidationErrorMessage("");
    onAdd({ name: trimmedName, url: trimmedUrl });
  };

  const handleClose = () => {
    setName("");
    setUrl("");
    setValidationErrorMessage("");
    onClose();
  };

  return (
    <Modal
      title="레포지토리 추가"
      onClose={handleClose}
      primaryLabel={isPending ? "추가 중..." : "추가"}
      onPrimaryClick={handlePrimary}
      isPrimaryDisabled={isPending || !name.trim() || !url.trim()}
    >
      <div className="flex flex-col gap-1">
        <label
          htmlFor="repo-name"
          className="typo-label text-(--color-text-secondary)"
        >
          이름
        </label>
        <input
          id="repo-name"
          value={name}
          onChange={(e) => {
            setName(e.target.value);
            if (validationErrorMessage) setValidationErrorMessage("");
          }}
          placeholder="레포지토리 이름"
          className="typo-body6 h-11 w-full rounded-full border border-white bg-white/50 px-4 py-3 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none"
          disabled={isPending}
        />

        <label
          htmlFor="repo-url"
          className="typo-label text-(--color-text-secondary)"
        >
          URL
        </label>
        <input
          id="repo-url"
          value={url}
          onChange={(e) => {
            setUrl(e.target.value);
            if (validationErrorMessage) setValidationErrorMessage("");
          }}
          placeholder="https://github.com/owner/repository"
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

export default RepositoryAddModal;
