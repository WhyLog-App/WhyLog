import IconArrowsReload from "@/assets/icons/arrow/ic_arrows_reload.svg?react";
import IconClock from "@/assets/icons/media/ic_clock.svg?react";
import IconGit from "@/assets/icons/media/ic_git.svg?react";
import { Icon } from "@/components/common/Icon";

interface GitPanelItemProps {
  repositoryId: number;
  name: string;
  updatedAtText: string;
  isActive?: boolean;
  isSyncing?: boolean;
  onSelect?: () => void;
  onSync?: (repositoryId: number) => void;
}

const GitPanelItem = ({
  repositoryId,
  name,
  updatedAtText,
  isActive = false,
  isSyncing = false,
  onSelect,
  onSync,
}: GitPanelItemProps) => {
  return (
    <div
      className={`flex w-full items-center justify-between rounded-lg p-3 ${
        isActive
          ? "bg-(--color-action-active)"
          : "bg-(--color-bg-surface) hover:bg-(--color-action-hover)"
      }`}
    >
      <button
        type="button"
        onClick={onSelect}
        aria-pressed={isActive}
        className="flex min-w-0 cursor-pointer items-center gap-0.5 text-left"
      >
        <Icon
          icon={IconGit}
          size={16}
          className="text-(--color-text-primary)"
        />
        <span className="typo-subtitle5 truncate text-(--color-text-primary)">
          {name}
        </span>
      </button>

      <div className="flex items-center gap-2">
        <span className="flex items-center gap-0.5">
          <Icon
            icon={IconClock}
            size={12}
            className="text-(--color-text-tertiary)"
          />
          <span className="typo-caption2 text-(--color-text-tertiary)">
            {updatedAtText}
          </span>
        </span>

        <button
          type="button"
          onClick={() => onSync?.(repositoryId)}
          disabled={isSyncing}
          aria-label="레포지토리 동기화"
          className="cursor-pointer disabled:opacity-50"
        >
          <Icon
            icon={IconArrowsReload}
            size={16}
            className={`text-(--color-text-secondary) ${
              isSyncing ? "animate-spin" : ""
            }`}
          />
        </button>
      </div>
    </div>
  );
};

export default GitPanelItem;
