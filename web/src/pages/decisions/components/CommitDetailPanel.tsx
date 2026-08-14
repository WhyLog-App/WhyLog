import IconArrowRight from "@/assets/icons/arrow/ic_arrow_right_md.svg?react";
import { Icon } from "@/components/common/Icon";
import { useGetCommitDetail } from "@/pages/git/hooks/useGetCommitDetail";
import { formatCommittedDate } from "@/utils/date";
import CommitHashBadge from "./CommitHashBadge";

export interface CommitDetailCommit {
  repositoryName: string;
  hash: string;
  message: string;
  commitId?: number;
  isConnected: boolean;
  reason?: string;
  repositoryId?: number;
  authorName?: string;
  committedDate?: string;
}

interface CommitDetailPanelProps {
  applicationName: string;
  keywords: string[];
  commit: CommitDetailCommit | null;
  collapsed: boolean;
  onToggle: () => void;
  onConnect: (commitId: number) => void;
  onUnlink: (commitId: number) => void;
  isPending: boolean;
}

const CommitDetailPanel = ({
  applicationName,
  keywords,
  commit,
  collapsed,
  onToggle,
  onConnect,
  onUnlink,
  isPending,
}: CommitDetailPanelProps) => {
  const detailQuery = useGetCommitDetail(
    collapsed ? null : (commit?.repositoryId ?? null),
    commit?.hash,
  );
  const detail = detailQuery.data;

  return (
    <aside
      className={`flex shrink-0 flex-col overflow-hidden rounded-xl border border-(--color-border-default) bg-[#F8FAFF] transition-[width] ${collapsed ? "w-12" : "w-84"}`}
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
              <p className="typo-subtitle4 leading-snug text-(--color-text-primary)">
                {commit.message}
              </p>
              <div className="flex items-center gap-2">
                <span className="typo-caption1 text-(--color-text-secondary)">
                  {commit.repositoryName}
                </span>
                <CommitHashBadge hash={commit.hash} />
              </div>
              <button
                type="button"
                disabled={!commit.commitId || isPending}
                onClick={() => {
                  if (commit.commitId == null) return;
                  if (commit.isConnected) onUnlink(commit.commitId);
                  else onConnect(commit.commitId);
                }}
                className={`w-full rounded-md px-3 py-2.5 typo-button-sm disabled:cursor-not-allowed disabled:opacity-60 ${
                  commit.isConnected
                    ? "bg-gray-200 text-(--color-text-secondary)"
                    : "bg-blue-600 text-white"
                }`}
              >
                {commit.isConnected ? "연결 취소" : "이 커밋 연결"}
              </button>
              <section className="flex flex-col gap-2 border-t border-(--color-border-default) pt-3">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  연결 미리보기
                </p>
                <div className="grid grid-cols-[minmax(0,1fr)_auto_minmax(0,1fr)] items-center gap-2 rounded-md border border-white bg-white px-3 py-2.5">
                  <div className="min-w-0 flex-1">
                    <p className="typo-caption1 text-(--color-text-tertiary)">
                      적용사항
                    </p>
                    <p className="line-clamp-2 typo-caption1 text-(--color-text-primary)">
                      {applicationName}
                    </p>
                  </div>
                  <Icon
                    icon={IconArrowRight}
                    size={14}
                    className="text-(--color-text-secondary)"
                  />
                  <div className="w-full rounded-md bg-(--color-bg-brand-subtle) px-2 py-1 text-left">
                    <p className="typo-caption1 text-(--color-text-brand)">
                      커밋
                    </p>
                    <p className="typo-caption1 text-(--color-text-brand)">
                      {commit.hash.slice(0, 7)}
                    </p>
                  </div>
                </div>
              </section>
              {commit.reason ? (
                <section className="flex flex-col gap-1 border-t border-(--color-border-default) pt-3">
                  <p className="typo-caption1 text-(--color-text-secondary)">
                    추천 이유
                  </p>
                  <p className="typo-caption1 leading-relaxed text-(--color-text-primary)">
                    {commit.reason}
                  </p>
                </section>
              ) : null}
              {keywords.length ? (
                <section className="flex flex-col gap-1.5 border-t border-(--color-border-default) pt-3">
                  <p className="typo-caption1 text-(--color-text-secondary)">
                    결정 키워드
                  </p>
                  <div className="flex flex-wrap gap-1">
                    {keywords.map((keyword) => (
                      <span
                        key={keyword}
                        className="rounded-full bg-[#EEF2FF] px-2 py-0.5 typo-caption1 text-(--color-text-secondary)"
                      >
                        #{keyword}
                      </span>
                    ))}
                  </div>
                </section>
              ) : null}
              {detail?.changed_file_list?.length ? (
                <section className="flex flex-col gap-2 border-t border-(--color-border-default) pt-3">
                  <div className="flex items-center justify-between">
                    <p className="typo-caption1 text-(--color-text-secondary)">
                      영향 파일 ({detail.changed_file_count})
                    </p>
                    <span className="typo-caption1 text-(--color-text-brand)">
                      모든 변경 파일 보기 ›
                    </span>
                  </div>
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
                </section>
              ) : null}
              <section className="flex flex-col gap-1.5 border-t border-(--color-border-default) pt-3">
                <p className="typo-caption1 text-(--color-text-secondary)">
                  커밋 정보
                </p>
                <div>
                  <p className="typo-caption1 text-(--color-text-tertiary)">
                    작성자
                  </p>
                  <p className="typo-caption1 text-(--color-text-primary)">
                    {detail?.author_name ?? commit.authorName ?? ""}
                    {detail?.author_email ? ` (${detail.author_email})` : ""}
                  </p>
                </div>
                <div>
                  <p className="typo-caption1 text-(--color-text-tertiary)">
                    작성 시각
                  </p>
                  <p className="typo-caption1 text-(--color-text-primary)">
                    {detail?.date_time
                      ? formatCommittedDate(detail.date_time)
                      : commit.committedDate
                        ? formatCommittedDate(commit.committedDate)
                        : ""}
                  </p>
                </div>
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
