import IconCalendar from "@/assets/icons/communication/ic_calendar.svg?react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import type { GitCommitItem } from "../../../types/git";

interface CommitTableRowProps {
  commit: GitCommitItem;
  onRowClick?: (commit: GitCommitItem) => void;
}

const decisionDotClassName = {
  success: "bg-(--color-green-500)",
  warning: "bg-(--color-yellow-500)",
  neutral: "bg-(--color-gray-400)",
} as const;

const shortenHash = (hash: string) => hash.slice(0, 7);

const shortenMessage = (message: string, visibleLength = 28) =>
  message.length > visibleLength
    ? `${message.slice(0, visibleLength)}...`
    : message;

const CommitTableRow = ({ commit, onRowClick }: CommitTableRowProps) => {
  return (
    <tr
      className="h-11 border-b border-(--color-light-700) cursor-pointer hover:bg-(--color-action-hover)"
      onClick={() => onRowClick?.(commit)}
    >
      <td className="px-2 py-0 align-middle">
        <span
          className="inline-flex w-[75px] max-w-full items-center justify-center truncate rounded-md bg-(--color-purple-50) px-[12px] py-[2px] text-[12px] font-medium tracking-[0.01em] text-(--color-purple-700)"
          title={commit.hash}
        >
          {shortenHash(commit.hash)}
        </span>
      </td>
      <td className="px-2 py-0 align-middle">
        <span
          className="block truncate typo-body5 font-semibold text-(--color-text-primary)"
          title={commit.message}
        >
          {shortenMessage(commit.message)}
        </span>
      </td>
      <td className="px-2 py-0 align-middle">
        <div className="flex min-w-0 items-center gap-2">
          {commit.decisionType === "neutral" ? (
            <span className="block truncate typo-body5 text-[rgba(0,0,0,0.3)]">
              -
            </span>
          ) : (
            <>
              <span
                className={`inline-flex h-2 w-2 shrink-0 rounded-full ${decisionDotClassName[commit.decisionType]}`}
              />
              <span className="block min-w-0 truncate typo-body5 text-(--color-text-secondary)">
                {commit.decisionText}
              </span>
            </>
          )}
        </div>
      </td>
      <td className="px-2 py-0 align-middle">
        <div className="flex min-w-0 items-center gap-2">
          {commit.authorProfileImage ? (
            <img
              src={commit.authorProfileImage}
              alt={commit.authorName}
              className="h-[14px] w-[14px] shrink-0 rounded-full object-cover"
            />
          ) : (
            <Icon
              icon={IconCircleUser}
              size={14}
              className="text-(--color-dark-100)"
            />
          )}
          <span className="block min-w-0 truncate typo-body5 text-(--color-text-secondary)">
            {commit.authorName}
          </span>
        </div>
      </td>
      <td className="px-2 py-0 align-middle">
        <div className="flex items-center gap-2 text-(--color-text-tertiary)">
          <Icon icon={IconCalendar} size={14} className="text-current" />
          <span className="typo-body5 text-(--color-text-tertiary)">
            {commit.dateText}
          </span>
        </div>
      </td>
      <td className="px-2 py-0 align-middle text-right">
        <span className="typo-body5 font-bold">
          <span className="text-[#05a660]">+{commit.changesAdded}</span>
          <span className="text-(--color-text-secondary) mx-1">-</span>
          <span className="text-[#e53535]">{commit.changesRemoved}</span>
        </span>
      </td>
    </tr>
  );
};

export default CommitTableRow;
