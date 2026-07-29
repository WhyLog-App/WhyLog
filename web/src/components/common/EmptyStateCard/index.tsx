import IconAddPlus from "@/assets/icons/edit/ic_add_plus.svg?react";
import EmptyStateIllustrationUrl from "@/assets/illustrations/empty_state_illustration.svg";
import { Icon } from "@/components/common/Icon";
import { CONTENT } from "./constants/content";
import type { EmptyStateCardProps } from "./types";

const EmptyStateCard = ({ page, onAction }: EmptyStateCardProps) => {
  const content = CONTENT[page];

  return (
    <div className="flex w-full max-w-150 flex-col items-center justify-center gap-9 overflow-hidden rounded-3xl border border-solid border-white bg-white/30 px-6 pt-12 pb-10 sm:px-12 sm:pt-14 sm:pb-12">
      {/* Illustration */}
      <img
        src={EmptyStateIllustrationUrl}
        alt="Empty state illustration"
        width={160}
        height={160}
      />

      {/* Text Group */}
      <div className="flex flex-col items-center gap-3 text-center">
        <h3 className="font-pretendard text-2xl font-bold leading-9 text-(--color-text-primary)">
          {content.title}
        </h3>
        <p className="typo-body5 text-(--color-text-secondary)">
          {content.description.map((line, _i) => (
            <span key={line} className="block">
              {line}
            </span>
          ))}
        </p>
      </div>

      {/* CTA Button */}
      <button
        type="button"
        onClick={onAction}
        className="flex cursor-pointer items-center gap-2 rounded-xl bg-linear-to-br from-[#5B8DEF] to-[#0063F7] px-7 py-3.5 shadow-[0px_4px_12px_0px_rgba(30,91,232,0.2)]"
      >
        <Icon icon={IconAddPlus} size={16} className="text-white" />
        <span className="typo-button-md text-(--color-text-inverse)">
          {content.buttonLabel}
        </span>
      </button>

      {/* Footnote */}
      <p className="typo-caption1 text-(--color-text-tertiary) text-center">
        {content.footnote}
      </p>
    </div>
  );
};

export default EmptyStateCard;
