import { useQueryClient } from "@tanstack/react-query";
import { useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { matchDecisionCommit } from "@/apis/decisions";
import IconArrowsReload from "@/assets/icons/arrow/ic_arrows_reload.svg?react";
import IconAddPlus from "@/assets/icons/edit/ic_add_plus.svg?react";
import { Icon } from "@/components/common/Icon";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import type {
  ApplicationConnectedCommit,
  ApplicationRecommendedCommit,
} from "@/types/application";
import type { CommitTab } from "@/types/decision";
import { formatCommittedDate } from "@/utils/date";
import { APPLICATION_CONNECTED_COMMITS_QUERY_KEY } from "../hooks/useConnectedCommits";
import { useLinkCommit } from "../hooks/useLinkCommit";
import { APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY } from "../hooks/useRecommendedCommits";
import { useUnlinkCommit } from "../hooks/useUnlinkCommit";
import CommitHashBadge from "./CommitHashBadge";
import { CommitMatchModal } from "./CommitMatchModal";
import GlassCard from "./GlassCard";

interface CommitTableCardProps {
  decisionId: number;
  applicationId: number;
  recommendedCommits: ApplicationRecommendedCommit[];
  linkedCommits: ApplicationConnectedCommit[];
  className?: string;
}

interface ReasonButtonProps {
  reason: string;
  isActive: boolean;
  onMouseEnter: () => void;
  onMouseLeave: () => void;
  onClick: () => void;
}

const ReasonButton = ({
  reason,
  isActive,
  onMouseEnter,
  onMouseLeave,
  onClick,
}: ReasonButtonProps) => {
  const buttonRef = useRef<HTMLButtonElement>(null);
  const [coords, setCoords] = useState<{ top: number; left: number } | null>(
    null,
  );

  useLayoutEffect(() => {
    if (!isActive || !buttonRef.current) {
      setCoords(null);
      return;
    }
    const updatePosition = () => {
      const rect = buttonRef.current?.getBoundingClientRect();
      if (!rect) return;
      setCoords({
        top: rect.top, // 버튼 위쪽 좌표 (tooltip은 translateY(-100%))
        left: rect.left + rect.width / 2,
      });
    };
    updatePosition();
    window.addEventListener("scroll", updatePosition, true);
    window.addEventListener("resize", updatePosition);
    return () => {
      window.removeEventListener("scroll", updatePosition, true);
      window.removeEventListener("resize", updatePosition);
    };
  }, [isActive]);

  return (
    <>
      <button
        ref={buttonRef}
        type="button"
        onMouseEnter={onMouseEnter}
        onMouseLeave={onMouseLeave}
        onClick={onClick}
        className="cursor-pointer typo-button-sm text-(--color-text-brand) hover:underline"
      >
        사유 보기
      </button>
      {isActive && coords
        ? createPortal(
            <div
              role="tooltip"
              style={{
                position: "fixed",
                top: coords.top,
                left: coords.left,
                transform: "translate(-50%, calc(-100% - 8px))",
                zIndex: 1000,
              }}
              className="pointer-events-none w-max max-w-44 whitespace-normal rounded-lg border border-(--color-border-default) bg-white px-3 py-2 shadow-lg"
            >
              <p
                className="typo-caption1 text-left leading-relaxed break-words"
                style={{ color: "#1c1c28" }}
              >
                {reason}
              </p>
              <span
                aria-hidden="true"
                className="absolute -bottom-1 left-1/2 size-2 -translate-x-1/2 rotate-45 border-r border-b border-(--color-border-default) bg-white"
              />
            </div>,
            document.body,
          )
        : null}
    </>
  );
};

const TabButton = ({
  active,
  children,
  onClick,
}: {
  active: boolean;
  children: React.ReactNode;
  onClick: () => void;
}) => (
  <button
    type="button"
    onClick={onClick}
    className={`cursor-pointer overflow-clip rounded-full px-4 py-0.5 typo-button-md transition-colors ${
      active
        ? "bg-blue-50 text-blue-700"
        : "text-(--color-text-secondary) hover:bg-(--color-action-hover)"
    }`}
  >
    {children}
  </button>
);

const CommitTableCard = ({
  decisionId,
  applicationId,
  recommendedCommits,
  linkedCommits,
  className = "",
}: CommitTableCardProps) => {
  const [tab, setTab] = useState<CommitTab>("recommended");
  const [hoveredReasonId, setHoveredReasonId] = useState<string | null>(null);
  const [pinnedReasonId, setPinnedReasonId] = useState<string | null>(null);
  const [isRefetching, setIsRefetching] = useState(false);
  const [isMatchModalOpen, setIsMatchModalOpen] = useState(false);
  const { teamId } = useCurrentTeam();
  const queryClient = useQueryClient();

  const handleRefresh = async () => {
    if (isRefetching) return;
    setIsRefetching(true);
    try {
      // 커밋 추천 매칭을 재실행한 뒤 최신 결과를 다시 불러온다.
      await matchDecisionCommit(decisionId);
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: [
            ...APPLICATION_RECOMMENDED_COMMITS_QUERY_KEY,
            applicationId,
          ],
        }),
        queryClient.invalidateQueries({
          queryKey: [...APPLICATION_CONNECTED_COMMITS_QUERY_KEY, applicationId],
        }),
      ]);
    } finally {
      setIsRefetching(false);
    }
  };
  const {
    linkCommits,
    pendingCommitIds,
    errorMessage: linkErrorMessage,
  } = useLinkCommit(applicationId);
  const {
    unlinkCommit,
    pendingCommitId: pendingUnlinkCommitId,
    errorMessage: unlinkErrorMessage,
  } = useUnlinkCommit(applicationId);
  const rowCount =
    tab === "recommended" ? recommendedCommits.length : linkedCommits.length;

  return (
    <GlassCard className={`gap-5 px-5 py-7 ${className}`}>
      <div className="flex items-center justify-between">
        <h2 className="typo-subtitle4 text-(--color-text-primary)">
          커밋 추천 결과
        </h2>
        <div className="flex items-center gap-1.5">
          <button
            type="button"
            aria-label="새로고침"
            onClick={handleRefresh}
            disabled={isRefetching}
            className="flex size-5 cursor-pointer items-center justify-center rounded bg-(--color-bg-surface) text-(--color-text-secondary) hover:bg-(--color-action-hover) disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Icon
              icon={IconArrowsReload}
              size={16}
              className={isRefetching ? "animate-spin" : undefined}
            />
          </button>
          <button
            type="button"
            aria-label="커밋 직접 매칭"
            onClick={() => setIsMatchModalOpen(true)}
            disabled={teamId == null}
            className="flex size-5 cursor-pointer items-center justify-center rounded bg-(--color-bg-surface) text-(--color-text-secondary) hover:bg-(--color-action-hover) disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Icon icon={IconAddPlus} size={16} />
          </button>
        </div>
      </div>

      <div className="flex items-start gap-1">
        <TabButton
          active={tab === "recommended"}
          onClick={() => setTab("recommended")}
        >
          추천 커밋
        </TabButton>
        <TabButton active={tab === "linked"} onClick={() => setTab("linked")}>
          연결된 커밋
        </TabButton>
      </div>

      <div className="scrollbar-thin w-full min-w-0 overflow-x-auto rounded-lg">
        <table className="w-full min-w-130 table-fixed border-collapse">
          <colgroup>
            <col className="w-[22%] min-w-30" />
            <col className="w-[14%] min-w-20" />
            <col className="w-auto" />
            <col className="w-[14%] min-w-18" />
            <col className="w-[12%] min-w-16" />
          </colgroup>
          <thead>
            <tr className="border-b border-(--color-border-default)">
              <th className="px-2 py-3 text-left typo-caption1 text-(--color-text-secondary) uppercase">
                Repository
              </th>
              <th className="px-2 py-3 text-left typo-caption1 text-(--color-text-secondary) uppercase">
                Commit
              </th>
              <th className="px-2 py-3 text-left typo-caption1 text-(--color-text-secondary) uppercase">
                Message
              </th>
              <th className="px-2 py-3 text-center typo-caption1 text-(--color-text-secondary) uppercase">
                {tab === "recommended" ? "추천 사유" : "커밋 일시"}
              </th>
              <th className="px-2 py-3 text-right typo-caption1 text-(--color-text-secondary) uppercase">
                연결
              </th>
            </tr>
          </thead>
          <tbody>
            {rowCount === 0 ? (
              <tr>
                <td
                  colSpan={5}
                  className="px-2 py-6 text-center typo-body6 text-(--color-text-tertiary)"
                >
                  표시할 커밋이 없습니다
                </td>
              </tr>
            ) : tab === "recommended" ? (
              recommendedCommits.map((c, idx) => {
                const isLast = idx === recommendedCommits.length - 1;
                const commitIdNum = Number(c.commit_id);
                const isCommitIdInvalid =
                  !c.commit_id || !Number.isFinite(commitIdNum);
                const isRowPending =
                  !isCommitIdInvalid &&
                  pendingCommitIds?.includes(commitIdNum) === true;
                return (
                  <tr
                    key={`rec-${c.commit_id}-${c.commit_hash}`}
                    className={
                      isLast ? "border-b border-(--color-border-default)" : ""
                    }
                  >
                    <td className="h-11 px-2 py-2.5">
                      <p className="truncate typo-body5 text-(--color-text-primary)">
                        {c.repository_name}
                      </p>
                    </td>
                    <td className="h-11 px-2 py-2.5">
                      <CommitHashBadge hash={c.commit_hash} />
                    </td>
                    <td className="h-11 px-2 py-2.5">
                      <p className="truncate typo-subtitle5 text-(--color-text-primary)">
                        {c.message}
                      </p>
                    </td>
                    <td className="h-11 px-2 py-2.5 text-center">
                      <ReasonButton
                        reason={c.reason}
                        isActive={
                          hoveredReasonId === c.commit_id ||
                          pinnedReasonId === c.commit_id
                        }
                        onMouseEnter={() => setHoveredReasonId(c.commit_id)}
                        onMouseLeave={() => setHoveredReasonId(null)}
                        onClick={() =>
                          setPinnedReasonId((prev) =>
                            prev === c.commit_id ? null : c.commit_id,
                          )
                        }
                      />
                    </td>
                    <td className="h-11 px-2 py-2.5 text-right">
                      <button
                        type="button"
                        onClick={() => linkCommits([commitIdNum])}
                        disabled={isRowPending || isCommitIdInvalid}
                        className="cursor-pointer rounded bg-(--color-bg-brand-subtle) px-3 py-0.5 typo-button-sm text-(--color-text-brand) hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {isRowPending ? "연결 중..." : "연결"}
                      </button>
                    </td>
                  </tr>
                );
              })
            ) : (
              linkedCommits.map((c, idx) => {
                const isLast = idx === linkedCommits.length - 1;
                const isRowPending = pendingUnlinkCommitId === c.commit_id;
                return (
                  <tr
                    key={`linked-${c.commit_id}`}
                    className={
                      isLast ? "border-b border-(--color-border-default)" : ""
                    }
                  >
                    <td className="h-11 px-2 py-2.5">
                      <p className="truncate typo-body5 text-(--color-text-primary)">
                        {c.repository_name}
                      </p>
                    </td>
                    <td className="h-11 px-2 py-2.5">
                      <CommitHashBadge hash={c.commit_hash} />
                    </td>
                    <td className="h-11 px-2 py-2.5">
                      <p className="truncate typo-subtitle5 text-(--color-text-primary)">
                        {c.message}
                      </p>
                    </td>
                    <td className="h-11 px-2 py-2.5 text-center">
                      <span className="typo-caption1 text-(--color-text-secondary)">
                        {formatCommittedDate(c.committed_date)}
                      </span>
                    </td>
                    <td className="h-11 px-2 py-2.5 text-right">
                      <button
                        type="button"
                        onClick={() => unlinkCommit(c.commit_id)}
                        disabled={isRowPending}
                        className="cursor-pointer rounded bg-(--color-bg-brand-subtle) px-3 py-0.5 typo-button-sm text-(--color-text-brand) hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {isRowPending ? "해제 중..." : "해제"}
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {linkErrorMessage || unlinkErrorMessage ? (
        <p className="typo-caption1 text-(--color-text-error)">
          {linkErrorMessage ?? unlinkErrorMessage}
        </p>
      ) : null}

      <div className="flex w-full items-start gap-5 typo-caption1 text-(--color-text-tertiary)"></div>

      {isMatchModalOpen && teamId != null ? (
        <CommitMatchModal
          applicationId={applicationId}
          teamId={teamId}
          onClose={() => setIsMatchModalOpen(false)}
        />
      ) : null}
    </GlassCard>
  );
};

export default CommitTableCard;
