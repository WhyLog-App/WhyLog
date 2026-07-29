import type { ComponentType, SVGProps } from "react";
import { Icon } from "@/components/common/Icon";
import type { RepositoryItem } from "@/types/git";

interface RepositorySidebarProps {
  repositories: RepositoryItem[];
  selectedRepoId: number | null;
  isLoading: boolean;
  isError: boolean;
  onSelect: (repoId: number) => void;
  IconArrowRight: ComponentType<SVGProps<SVGSVGElement>>;
}

const RepositorySidebar = ({
  repositories,
  selectedRepoId,
  isLoading,
  isError,
  onSelect,
  IconArrowRight,
}: RepositorySidebarProps) => {
  return (
    <aside className="flex w-60 shrink-0 flex-col gap-3 border-r border-(--color-border-default) bg-(--color-bg-surface)/40 px-4 py-5">
      <p className="typo-caption1 px-2 text-(--color-text-tertiary) uppercase">
        Repository
      </p>

      <ul className="flex min-h-0 flex-1 flex-col gap-1 overflow-y-auto">
        {isLoading ? (
          <li className="px-2 py-2 typo-body6 text-(--color-text-tertiary)">
            불러오는 중...
          </li>
        ) : isError ? (
          <li className="px-2 py-2 typo-body6 text-(--color-text-error)">
            레포지토리를 불러오지 못했습니다
          </li>
        ) : repositories.length === 0 ? (
          <li className="px-2 py-2 typo-body6 text-(--color-text-tertiary)">
            연동된 레포지토리가 없습니다
          </li>
        ) : (
          repositories.map((repo) => {
            const isSelected = repo.repository_id === selectedRepoId;
            return (
              <li key={repo.repository_id}>
                <button
                  type="button"
                  onClick={() => onSelect(repo.repository_id)}
                  className={`flex w-full cursor-pointer items-center justify-between rounded-lg px-3 py-2 typo-body5 transition-colors ${
                    isSelected
                      ? "bg-(--color-bg-brand-subtle) text-(--color-text-brand)"
                      : "text-(--color-text-primary) hover:bg-(--color-action-hover)"
                  }`}
                >
                  <span className="truncate">{repo.name}</span>
                  {isSelected ? (
                    <Icon
                      icon={IconArrowRight}
                      size={16}
                      className="text-(--color-text-brand)"
                    />
                  ) : null}
                </button>
              </li>
            );
          })
        )}
      </ul>
    </aside>
  );
};

export default RepositorySidebar;
