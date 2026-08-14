import IconArrowRight from "@/assets/icons/arrow/ic_arrow_right_md.svg?react";
import { Icon } from "@/components/common/Icon";
import { useGetCommitDetail } from "@/pages/git/hooks/useGetCommitDetail";
import { formatCommittedDate } from "@/utils/date";
import CommitHashBadge from "./CommitHashBadge";

export interface CommitDetailCommit {
  repositoryName: string;
  hash: string;
  message: string;
  reason?: string;
  repositoryId?: number;
  authorName?: string;
  committedDate?: string;
  addedLines?: number;
  removedLines?: number;
}

interface CommitDetailPanelProps {
  commit: CommitDetailCommit | null;
  collapsed: boolean;
  onToggle: () => void;
}

const CommitDetailPanel = ({
  commit,
  collapsed,
  onToggle,
}: CommitDetailPanelProps) => {
  const detailQuery = useGetCommitDetail(
    collapsed ? null : (commit?.repositoryId ?? null),
    commit?.hash,
  );
  const detail = detailQuery.data;

  return (
    <aside
      className={`flex shrink-0 flex-col overflow-hidden bg-(--color-bg-brand-subtle) transition-[width] ${collapsed ? "w-12" : "w-84"}`}
    >
      <div className="flex items-center justify-between border-b border-(--color-border-default) px-4 py-3">
        {!collapsed ? (
          <h2 className="typo-subtitle4 text-(--color-text-primary)">
            커밋 상세
          </h2>
        ) : null}
        <button
          type="button"
          onClick={onToggle}
          aria-label={collapsed ? "상세 패널 펼치기" : "상세 패널 접기"}
          className="ml-auto flex size-6 items-center justify-center rounded text-(--color-text-secondary) hover:bg-(--color-action-hover)"
        >
          <Icon
            icon={IconArrowRight}
            size={16}
            className={collapsed ? "rotate-180" : ""}
          />
        </button>
      </div>
      {!collapsed ? (
        <div className="application-scroll flex flex-1 flex-col gap-3 overflow-y-auto p-4">
          {commit ? (
            <>
              <div className="flex items-center gap-2">
                <span className="typo-caption1 text-(--color-text-secondary)">
                  {commit.repositoryName}
                </span>
                <CommitHashBadge hash={commit.hash} />
              </div>
              <p className="typo-subtitle5 leading-snug text-(--color-text-primary)">
                {commit.message}
              </p>
              <button
                type="button"
                className="w-full rounded-md bg-blue-600 px-3 py-2 typo-button-sm text-white shadow-sm"
              >
                이 커밋 연결
              </button>
              <section className="flex flex-col gap-2">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  연결 미리보기
                </p>
                <div className="flex items-center gap-2 rounded-md bg-blue-100/70 px-3 py-2 typo-caption1 text-(--color-text-secondary)">
                  <span className="max-w-24 truncate">결정 사항</span>
                  <Icon icon={IconArrowRight} size={14} />
                  <CommitHashBadge hash={commit.hash} />
                </div>
              </section>
              <section className="flex flex-col gap-1">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  추천 이유
                </p>
                <p className="typo-caption1 leading-relaxed text-(--color-text-primary)">
                  {commit.reason ?? "선택한 커밋을 결정사항에 연결합니다."}
                </p>
              </section>
              <section className="flex flex-col gap-2">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  결정 키워드
                </p>
                <div className="flex flex-wrap gap-1">
                  <span className="rounded-full bg-white px-2 py-0.5 typo-caption1 text-(--color-text-brand)">
                    # 결정 사항
                  </span>
                  <span className="rounded-full bg-white px-2 py-0.5 typo-caption1 text-(--color-text-brand)">
                    # 코드 반영
                  </span>
                </div>
              </section>
              <section className="flex flex-col gap-2">
                <div className="flex items-center justify-between">
                  <p className="typo-caption1 text-(--color-text-secondary)">
                    영향 파일{detail ? ` (${detail.changed_file_count})` : ""}
                  </p>
                  {detail?.changed_file_list?.length ? (
                    <span className="typo-caption1 text-(--color-text-brand)">
                      모든 변경 파일 보기
                    </span>
                  ) : null}
                </div>
                {detail?.changed_file_list?.length ? (
                  <div className="flex flex-col gap-2">
                    {detail.changed_file_list.slice(0, 3).map((file) => (
                      <div
                        key={file.file_name}
                        className="flex items-center gap-2 typo-caption1"
                      >
                        <span className="min-w-0 flex-1 truncate text-(--color-text-secondary)">
                          {file.file_name}
                        </span>
                        <span className="text-(--color-green-500)">
                          +{file.added_lines}
                        </span>
                        <span className="text-(--color-text-error)">
                          -{file.deleted_lines}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="flex items-center justify-between rounded-md bg-white px-3 py-2 typo-caption1 text-(--color-text-secondary)">
                    <span>변경 파일 정보 없음</span>
                    {commit.addedLines != null ||
                    commit.removedLines != null ? (
                      <span>
                        <span className="text-(--color-green-500)">
                          +{commit.addedLines ?? 0}
                        </span>
                        <span className="ml-1 text-(--color-text-error)">
                          -{commit.removedLines ?? 0}
                        </span>
                      </span>
                    ) : null}
                  </div>
                )}
              </section>
              <section className="flex flex-col gap-1 border-t border-(--color-border-default) pt-2">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  커밋 정보
                </p>
                <p className="typo-caption1 text-(--color-text-tertiary)">
                  {detail?.author_email ?? commit.repositoryName}
                </p>
                <p className="typo-body6 text-(--color-text-primary)">
                  {detail?.author_name ?? commit.authorName ?? "-"}
                </p>
                <p className="typo-caption1 text-(--color-text-secondary)">
                  {detail?.date_time
                    ? formatCommittedDate(detail.date_time)
                    : commit.committedDate
                      ? formatCommittedDate(commit.committedDate)
                      : "-"}
                </p>
              </section>
            </>
          ) : (
            <p className="py-10 text-center typo-body6 text-(--color-text-tertiary)">
              커밋을 선택하세요
            </p>
          )}
        </div>
      ) : null}
    </aside>
  );
};

export default CommitDetailPanel;
