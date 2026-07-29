import {
  type ComponentType,
  type ReactNode,
  type SVGProps,
  useEffect,
  useRef,
} from "react";
import { Icon } from "@/components/common/Icon";
import type { RepositoryCommitItem } from "@/types/git";

interface CommitSelectPanelProps {
  repoName: string;
  commits: RepositoryCommitItem[];
  isLoading: boolean;
  isError: boolean;
  hasNextPage: boolean;
  isFetchingNextPage: boolean;
  fetchNextPage: () => void;
  keyword: string;
  onKeywordChange: (value: string) => void;
  selectedCommitIds: Set<number>;
  onToggleCommit: (commitId: number) => void;
  renderHash: (hash: string) => ReactNode;
  formatDate: (iso?: string) => string;
  IconSearch: ComponentType<SVGProps<SVGSVGElement>>;
}

const CommitSelectPanel = ({
  repoName,
  commits,
  isLoading,
  isError,
  hasNextPage,
  isFetchingNextPage,
  fetchNextPage,
  keyword,
  onKeywordChange,
  selectedCommitIds,
  onToggleCommit,
  renderHash,
  formatDate,
  IconSearch,
}: CommitSelectPanelProps) => {
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const scrollRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    const root = scrollRef.current;
    if (!sentinel || !root) return;
    if (!hasNextPage) return;

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting && hasNextPage && !isFetchingNextPage) {
            fetchNextPage();
          }
        }
      },
      { root, rootMargin: "80px" },
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  return (
    <section className="flex min-w-0 flex-1 flex-col">
      {/* Panel Header */}
      <div className="flex items-center justify-between gap-4 px-5 py-4">
        <p className="typo-body5 text-(--color-text-primary)">
          {repoName ? (
            <>
              <span className="font-medium">{repoName}</span>
              <span className="px-2 text-(--color-text-tertiary)">/</span>
              <span className="text-(--color-text-secondary)">
                {commits.length} commits
              </span>
            </>
          ) : (
            <span className="text-(--color-text-tertiary)">
              레포지토리를 선택하세요
            </span>
          )}
        </p>

        <div className="relative flex w-64 items-center">
          <span className="pointer-events-none absolute left-3 flex items-center justify-center text-(--color-text-tertiary)">
            <Icon icon={IconSearch} size={16} />
          </span>
          <input
            type="text"
            value={keyword}
            onChange={(e) => onKeywordChange(e.target.value)}
            placeholder="커밋 메시지 또는 해시 검색"
            className="w-full rounded-lg border border-(--color-border-default) bg-white py-2 pr-3 pl-9 typo-body6 text-(--color-text-primary) placeholder:text-(--color-text-tertiary) focus:outline-none focus:ring-2 focus:ring-(--color-border-brand)"
          />
        </div>
      </div>

      {/* Table */}
      <div
        ref={scrollRef}
        className="scrollbar-thin min-h-0 flex-1 overflow-y-auto px-5"
      >
        <table className="w-full table-fixed border-collapse">
          <colgroup>
            <col className="w-10" />
            <col className="w-24" />
            <col className="w-auto" />
            <col className="w-24" />
          </colgroup>
          <thead className="sticky top-0 bg-(--color-bg-subtle)">
            <tr className="border-b border-(--color-border-default)">
              <th className="px-2 py-3" />
              <th className="px-2 py-3 text-left typo-caption1 text-(--color-text-secondary)">
                Commit
              </th>
              <th className="px-2 py-3 text-left typo-caption1 text-(--color-text-secondary)">
                Message
              </th>
              <th className="px-2 py-3 text-right typo-caption1 text-(--color-text-secondary)">
                날짜
              </th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td
                  colSpan={4}
                  className="px-2 py-6 text-center typo-body6 text-(--color-text-tertiary)"
                >
                  불러오는 중...
                </td>
              </tr>
            ) : isError ? (
              <tr>
                <td
                  colSpan={4}
                  className="px-2 py-6 text-center typo-body6 text-(--color-text-error)"
                >
                  커밋을 불러오지 못했습니다
                </td>
              </tr>
            ) : commits.length === 0 ? (
              <tr>
                <td
                  colSpan={4}
                  className="px-2 py-6 text-center typo-body6 text-(--color-text-tertiary)"
                >
                  {keyword ? "검색 결과가 없습니다" : "표시할 커밋이 없습니다"}
                </td>
              </tr>
            ) : (
              commits.map((c) => {
                const isConnected = c.connected_application != null;
                const isChecked = selectedCommitIds.has(c.commit_id);
                const isDisabled = isConnected;
                return (
                  <tr
                    key={c.commit_id}
                    className={`border-b border-(--color-border-default)/60 ${
                      isDisabled ? "opacity-50" : ""
                    }`}
                  >
                    <td className="px-2 py-2.5">
                      <input
                        type="checkbox"
                        checked={isChecked}
                        disabled={isDisabled}
                        onChange={() => onToggleCommit(c.commit_id)}
                        className="size-4 cursor-pointer rounded border-(--color-border-default) accent-(--color-text-brand) disabled:cursor-not-allowed"
                        aria-label={`커밋 ${c.hash.slice(0, 6)} 선택`}
                      />
                    </td>
                    <td className="px-2 py-2.5">{renderHash(c.hash)}</td>
                    <td className="px-2 py-2.5">
                      <p className="truncate typo-body5 text-(--color-text-primary)">
                        {c.message}
                      </p>
                      {isConnected && c.connected_application ? (
                        <p className="typo-caption1 text-(--color-text-tertiary)">
                          · {c.connected_application.name}에 연결됨
                        </p>
                      ) : null}
                    </td>
                    <td className="px-2 py-2.5 text-right typo-caption1 text-(--color-text-secondary)">
                      {formatDate(c.date_time)}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* Infinite scroll sentinel */}
        {hasNextPage ? (
          <div
            ref={sentinelRef}
            className="flex items-center justify-center py-4 typo-caption1 text-(--color-text-tertiary)"
          >
            {isFetchingNextPage ? "불러오는 중..." : ""}
          </div>
        ) : null}
      </div>
    </section>
  );
};

export default CommitSelectPanel;
