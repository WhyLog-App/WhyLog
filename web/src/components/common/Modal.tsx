import { type ReactNode, useEffect, useId } from "react";
import { createPortal } from "react-dom";
import IconCloseMd from "@/assets/icons/menu/ic_close_md.svg?react";
import { Icon } from "@/components/common/Icon";

interface ModalProps {
  title: string;
  onClose: () => void;
  primaryLabel: string;
  onPrimaryClick: () => void;
  isPrimaryDisabled?: boolean;
  children: ReactNode;
}

const Modal = ({
  title,
  onClose,
  primaryLabel,
  onPrimaryClick,
  isPrimaryDisabled = false,
  children,
}: ModalProps) => {
  const titleId = useId();

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
    >
      {/* Dim Overlay */}
      <button
        type="button"
        className="absolute inset-0 bg-(--color-overlay-dim)"
        onClick={onClose}
        aria-label="모달 닫기"
      />

      {/* Modal */}
      <div className="relative flex w-110 flex-col gap-7 overflow-hidden rounded-3xl border border-(--color-border-default) bg-(--color-bg-subtle) px-9 pt-8 pb-9 shadow-[0px_2px_8px_0px_rgba(40,41,61,0.08),0px_20px_32px_0px_rgba(96,97,112,0.24)]">
        {/* Header */}
        <div className="flex items-center justify-between">
          <h3 id={titleId} className="typo-h5 text-(--color-text-primary)">
            {title}
          </h3>
          <button
            type="button"
            className="flex cursor-pointer items-center justify-center rounded-lg py-1"
            onClick={onClose}
            aria-label="닫기"
          >
            <Icon
              icon={IconCloseMd}
              size={24}
              className="text-(--color-text-primary)"
            />
          </button>
        </div>

        {/* Content */}
        {children}

        {/* Button Group */}
        <div className="flex gap-3">
          <button
            type="button"
            className="typo-button-md flex flex-1 cursor-pointer items-center justify-center rounded-xl border-[1.5px] border-white bg-white/50 px-7 py-3.5 text-(--color-text-secondary)"
            onClick={onClose}
          >
            취소
          </button>
          <button
            type="button"
            className="typo-button-md flex flex-1 cursor-pointer items-center justify-center rounded-xl px-7 py-3.5 text-(--color-text-inverse) shadow-[0px_4px_12px_0px_rgba(30,91,232,0.2)] disabled:opacity-50 disabled:cursor-not-allowed"
            style={{
              backgroundImage:
                "linear-gradient(147deg, #5b8def 15.47%, #0063f7 84.42%)",
            }}
            onClick={onPrimaryClick}
            disabled={isPrimaryDisabled}
          >
            {primaryLabel}
          </button>
        </div>
      </div>
    </div>,
    document.body,
  );
};

export default Modal;
