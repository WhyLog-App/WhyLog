import { formatCommittedDate } from "@/utils/date";
import CommitHashBadge from "./CommitHashBadge";

export type CommitCardVariant = "recommended" | "direct" | "linked";

interface CommitCardProps {
  hash: string;
  message: string;
  repositoryName: string;
  variant: CommitCardVariant;
  reason?: string;
  committedDate?: string;
  authorName?: string;
  addedLines?: number;
  removedLines?: number;
  selected?: boolean;
  onClick?: () => void;
  onDragStart?: () => void;
  onDragEnd?: () => void;
}

const CommitCard = ({
  hash,
  message,
  repositoryName,
  variant,
  reason,
  committedDate,
  authorName,
  addedLines,
  removedLines,
  selected = false,
  onClick,
  onDragStart,
  onDragEnd,
}: CommitCardProps) => (
  <button
    type="button"
    onClick={onClick}
    draggable={onDragStart != null}
    onDragStart={onDragStart}
    onDragEnd={onDragEnd}
    className={`flex w-full flex-col gap-1 border-b px-2 py-2.5 text-left transition-colors ${
      selected
        ? "mx-1 max-w-[calc(100%-0.5rem)] rounded-lg border-b-0 bg-(--color-bg-brand-subtle)"
        : "border-(--color-border-default) bg-(--color-bg-surface) hover:mx-1 hover:max-w-[calc(100%-0.5rem)] hover:rounded-lg hover:border-b-0 hover:bg-(--color-bg-brand-subtle)"
    }`}
  >
    <div className="flex min-w-0 items-center gap-1.5">
      {variant !== "linked" ? (
        <span
          aria-hidden="true"
          className="size-3 rounded border border-(--color-border-default)"
        />
      ) : null}
      <p className="min-w-0 truncate typo-subtitle5 text-(--color-text-primary)">
        {message}
      </p>
    </div>
    <div
      className={`flex min-w-0 gap-1.5 overflow-hidden whitespace-nowrap ${variant === "linked" ? "" : "pl-4.5"}`}
    >
      <span className="max-w-24 shrink-0 truncate rounded bg-(--color-bg-subtle) px-1.5 py-0.5 typo-caption1 text-(--color-text-secondary)">
        {repositoryName}
      </span>
      <CommitHashBadge hash={hash} />
      {authorName ? (
        <span className="shrink-0 typo-caption1 text-(--color-text-tertiary)">
          {authorName}
        </span>
      ) : null}
      {committedDate ? (
        <span className="shrink-0 typo-caption1 text-(--color-text-tertiary)">
          {formatCommittedDate(committedDate)}
        </span>
      ) : null}
      {addedLines != null || removedLines != null ? (
        <span className="shrink-0 typo-caption1">
          {addedLines != null ? (
            <span className="text-(--color-green-500)">+{addedLines}</span>
          ) : null}
          {removedLines != null ? (
            <span className="ml-1 text-(--color-red-700)">-{removedLines}</span>
          ) : null}
        </span>
      ) : null}
    </div>
    {variant === "recommended" && reason ? (
      <p className="min-w-0 truncate pl-4.5 typo-caption1 text-(--color-text-secondary)">
        {reason}
      </p>
    ) : null}
  </button>
);

export default CommitCard;
