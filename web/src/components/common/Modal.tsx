import { type ReactNode, useEffect, useId, useRef } from "react";
import { createPortal } from "react-dom";
import IconCloseMd from "@/assets/icons/menu/ic_close_md.svg?react";
import { Icon } from "@/components/common/Icon";

interface ModalProps {
  title: string;
  onClose: () => void;
  primaryLabel: string;
  onPrimaryClick: () => void;
  isPrimaryDisabled?: boolean;
  showCancel?: boolean;
  isDismissible?: boolean;
  children: ReactNode;
}

const FOCUSABLE_SELECTOR = [
  'a[href]:not([tabindex="-1"])',
  'button:not([disabled]):not([tabindex="-1"])',
  'input:not([disabled]):not([tabindex="-1"])',
  'select:not([disabled]):not([tabindex="-1"])',
  'textarea:not([disabled]):not([tabindex="-1"])',
  '[tabindex]:not([tabindex="-1"])',
].join(",");

const Modal = ({
  title,
  onClose,
  primaryLabel,
  onPrimaryClick,
  isPrimaryDisabled = false,
  showCancel = true,
  isDismissible = true,
  children,
}: ModalProps) => {
  const titleId = useId();
  const dialogRef = useRef<HTMLDivElement>(null);
  const onCloseRef = useRef(onClose);

  useEffect(() => {
    onCloseRef.current = onClose;
  }, [onClose]);

  useEffect(() => {
    const dialog = dialogRef.current;
    const previouslyFocused =
      document.activeElement instanceof HTMLElement
        ? document.activeElement
        : null;
    const focusableElements = () =>
      Array.from(
        dialog?.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR) ?? [],
      );

    (focusableElements()[0] ?? dialog)?.focus();

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape" && isDismissible) {
        onCloseRef.current();
        return;
      }
      if (e.key !== "Tab" || !dialog) return;

      const elements = focusableElements();
      if (elements.length === 0) {
        e.preventDefault();
        dialog.focus();
        return;
      }

      const first = elements[0];
      const last = elements[elements.length - 1];
      const activeElement = document.activeElement;
      if (
        e.shiftKey &&
        (activeElement === first || !dialog.contains(activeElement))
      ) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      previouslyFocused?.focus();
    };
  }, [isDismissible]);

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      ref={dialogRef}
      tabIndex={-1}
    >
      {isDismissible ? (
        <button
          type="button"
          className="absolute inset-0 bg-(--color-overlay-dim)"
          onClick={onClose}
          aria-label="모달 닫기"
          tabIndex={-1}
        />
      ) : (
        <div className="absolute inset-0 bg-(--color-overlay-dim)" />
      )}

      <div className="relative flex max-h-[calc(100dvh-2rem)] w-full max-w-110 flex-col gap-5 overflow-y-auto rounded-2xl border border-(--color-border-default) bg-(--color-bg-subtle) px-5 py-6 shadow-[0px_2px_8px_0px_rgba(40,41,61,0.08),0px_20px_32px_0px_rgba(96,97,112,0.24)] sm:gap-7 sm:rounded-3xl sm:px-9 sm:pt-8 sm:pb-9">
        <div className="flex items-center justify-between">
          <h3 id={titleId} className="typo-h5 text-(--color-text-primary)">
            {title}
          </h3>
          {isDismissible && (
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
          )}
        </div>

        {children}

        <div className="flex gap-3">
          {showCancel && (
            <button
              type="button"
              className="typo-button-md flex flex-1 cursor-pointer items-center justify-center rounded-xl border-[1.5px] border-white bg-white/50 px-7 py-3.5 text-(--color-text-secondary)"
              onClick={onClose}
            >
              취소
            </button>
          )}
          <button
            type="button"
            className="typo-button-md flex flex-1 cursor-pointer items-center justify-center rounded-xl bg-(--color-action-primary) px-7 py-3.5 text-(--color-text-inverse) shadow-[0px_4px_12px_0px_rgba(30,91,232,0.2)] disabled:cursor-not-allowed disabled:opacity-50"
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
