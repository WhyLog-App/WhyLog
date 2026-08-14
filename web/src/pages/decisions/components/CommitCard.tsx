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
        ? "border-(--color-border-brand) bg-(--color-bg-brand-subtle)"
        : "border-(--color-border-default) bg-(--color-bg-surface) hover:bg-(--color-action-hover)"
    }`}
  >
    <div className="flex items-center gap-1.5">
      {variant !== "linked" ? (
        <span
          aria-hidden="true"
          className="size-3 rounded border border-(--color-border-default)"
        />
      ) : null}
      <p className="truncate typo-subtitle5 text-(--color-text-primary)">
        {message}
      </p>
    </div>
    <div
      className={`flex items-center gap-1.5 ${variant === "linked" ? "" : "pl-4.5"}`}
    >
      <span className="max-w-24 truncate rounded bg-(--color-bg-subtle) px-1.5 py-0.5 typo-caption1 text-(--color-text-secondary)">
        {repositoryName}
      </span>
      <CommitHashBadge hash={hash} />
      {authorName ? (
        <span className="truncate typo-caption1 text-(--color-text-tertiary)">
          {authorName}
        </span>
      ) : null}
      {committedDate ? (
        <span className="whitespace-nowrap typo-caption1 text-(--color-text-tertiary)">
          {formatCommittedDate(committedDate)}
        </span>
      ) : null}
      {addedLines != null || removedLines != null ? (
        <span className="ml-auto whitespace-nowrap typo-caption1">
          <span className="text-(--color-green-500)">+{addedLines ?? 0}</span>
          <span className="ml-1 text-(--color-text-error)">
            -{removedLines ?? 0}
          </span>
        </span>
      ) : null}
    </div>
    {variant === "recommended" && reason ? (
      <p className="line-clamp-1 pl-4.5 typo-caption1 text-(--color-text-secondary)">
        {reason}
      </p>
    ) : null}
  </button>
);

export default CommitCard;
