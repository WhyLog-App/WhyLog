import IconCalendar from "@/assets/icons/communication/ic_calendar.svg?react";
import IconNoteSearch from "@/assets/icons/file/ic_note_search.svg?react";
import IconNotebook from "@/assets/icons/file/ic_notebook.svg?react";
import IconGit from "@/assets/icons/media/ic_git.svg?react";
import IconCircleCheck from "@/assets/icons/warning/ic_circle_check.svg?react";
import IconCircleHelp from "@/assets/icons/warning/ic_circle_help.svg?react";
import { Icon, type IconSource } from "@/components/common/Icon";
import type { ApplicationConnectedCommit } from "@/types/application";
import type { DecisionConfidence, DecisionDetail } from "@/types/decision";
import { formatCommitDate } from "@/utils/date";
import CommitHashBadge from "./CommitHashBadge";
import GlassCard from "./GlassCard";

interface OverviewTabProps {
  detail: DecisionDetail;
  confidence: DecisionConfidence;
  linkedCommits: ApplicationConnectedCommit[];
  onOpenCode: () => void;
  onOpenContext: () => void;
}

interface MetricCardProps {
  icon: IconSource;
  label: string;
  value: string;
}

const MetricCard = ({ icon, label, value }: MetricCardProps) => (
  <GlassCard className="flex-row items-center gap-3.5 px-5 py-4">
    <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-(--color-bg-brand-subtle) text-(--color-text-brand)">
      <Icon icon={icon} size={20} />
    </span>
    <div className="flex min-w-0 flex-col gap-1">
      <p className="typo-body6 text-(--color-text-tertiary)">{label}</p>
      <p className="typo-subtitle2 text-(--color-text-primary)">{value}</p>
    </div>
  </GlassCard>
);

const OverviewTab = ({
  detail,
  confidence,
  linkedCommits,
  onOpenCode,
  onOpenContext,
}: OverviewTabProps) => {
  const metrics: MetricCardProps[] = [
    {
      icon: IconCircleHelp,
      label: "결정 신뢰도",
      value: confidence.score == null ? "—" : `${confidence.score}%`,
    },
    {
      icon: IconNoteSearch,
      label: "핵심 근거 수",
      value: `${detail.decision_reasons.length}개`,
    },
    {
      icon: IconGit,
      label: "연결 커밋 수",
      value: `${linkedCommits.length}개`,
    },
    { icon: IconNotebook, label: "관련 리소스 수", value: "—" },
  ];

  return (
    <section className="flex min-h-0 flex-col gap-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {metrics.map((metric) => (
          <MetricCard key={metric.label} {...metric} />
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-5">
        <GlassCard className="min-h-64 gap-4 p-5 xl:col-span-2">
          <h2 className="typo-body5 font-bold text-(--color-text-primary)">
            결정 요약
          </h2>
          {detail.decision_reasons.length ? (
            <ul className="flex flex-col gap-2.5">
              {detail.decision_reasons.map((reason) => (
                <li
                  key={reason.reason_id}
                  className="flex items-start gap-2 typo-body6 leading-relaxed text-(--color-text-secondary)"
                >
                  <span
                    aria-hidden
                    className="font-bold text-(--color-text-tertiary)"
                  >
                    •
                  </span>
                  <span>{reason.title}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="typo-body6 text-(--color-text-tertiary)">
              등록된 결정 근거가 없습니다.
            </p>
          )}
        </GlassCard>

        <GlassCard className="min-w-0 gap-3.5 p-5 xl:col-span-3">
          <h2 className="typo-body5 font-bold text-(--color-text-primary)">
            최근 반영 현황
          </h2>
          <div className="overflow-x-auto">
            <table className="w-full min-w-150 table-fixed border-collapse">
              <colgroup>
                <col className="w-23" />
                <col />
                <col className="w-22" />
                <col className="w-36" />
              </colgroup>
              <thead className="border-b border-(--color-border-default)">
                <tr className="typo-caption2 text-left text-(--color-text-secondary)">
                  <th className="px-2 py-3 font-normal">COMMIT</th>
                  <th className="px-2 py-3 font-normal">MESSAGE</th>
                  <th className="px-2 py-3 font-normal">AUTHOR</th>
                  <th className="px-2 py-3 font-normal">DATE</th>
                </tr>
              </thead>
              <tbody>
                {linkedCommits.slice(0, 6).map((commit) => (
                  <tr key={commit.commit_id} className="typo-caption1">
                    <td className="h-11 px-2 py-2">
                      <CommitHashBadge hash={commit.commit_hash} />
                    </td>
                    <td className="h-11 px-2 py-2">
                      <p className="truncate typo-subtitle5 text-(--color-text-primary)">
                        {commit.message}
                      </p>
                    </td>
                    <td className="h-11 truncate px-2 py-2 text-(--color-text-secondary)">
                      {commit.author_name}
                    </td>
                    <td className="h-11 px-2 py-2">
                      <span className="flex items-center gap-1.5 whitespace-nowrap text-(--color-text-tertiary)">
                        <Icon icon={IconCalendar} size={14} />
                        {formatCommitDate(commit.committed_date)}
                      </span>
                    </td>
                  </tr>
                ))}
                {linkedCommits.length === 0 ? (
                  <tr>
                    <td
                      colSpan={4}
                      className="px-2 py-6 text-center typo-body6 text-(--color-text-tertiary)"
                    >
                      연결된 커밋이 없습니다.
                    </td>
                  </tr>
                ) : null}
              </tbody>
            </table>
          </div>
          <button
            type="button"
            onClick={onOpenCode}
            className="ml-auto cursor-pointer typo-caption1 font-medium text-(--color-text-brand) hover:underline"
          >
            더보기
          </button>
        </GlassCard>
      </div>

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-5">
        <GlassCard className="gap-4 p-5 xl:col-span-2">
          <h2 className="typo-body5 font-bold text-(--color-text-primary)">
            논의 흐름
          </h2>
          <ol className="flex flex-col">
            {detail.decision_timelines.slice(0, 3).map((item, index, items) => (
              <li
                key={`${item.time}-${item.step}-${item.content}`}
                className="flex gap-3"
              >
                <div className="flex w-5.5 shrink-0 flex-col items-center">
                  <span className="z-10 flex size-5.5 shrink-0 items-center justify-center rounded-full bg-(--color-action-primary) typo-caption2 font-bold text-(--color-text-inverse)">
                    {index + 1}
                  </span>
                  {index < items.length - 1 ? (
                    <span className="h-10 w-0.5 bg-(--color-border-default)" />
                  ) : null}
                </div>
                <div className="min-w-0 pb-4">
                  <div className="flex items-center gap-2">
                    <span className="typo-caption1 text-(--color-text-tertiary)">
                      {item.time}
                    </span>
                    <strong className="typo-body6 text-(--color-text-primary)">
                      {item.step}
                    </strong>
                  </div>
                  <p className="mt-1 typo-caption1 leading-relaxed text-(--color-text-tertiary)">
                    {item.content}
                  </p>
                </div>
              </li>
            ))}
          </ol>
        </GlassCard>

        <GlassCard className="gap-4 p-5 xl:col-span-3">
          <h2 className="typo-body5 font-bold text-(--color-text-primary)">
            결정근거
          </h2>
          <ul className="flex flex-col gap-3">
            {detail.decision_reasons.map((reason) => (
              <li
                key={reason.reason_id}
                className="flex items-start gap-2 px-2 py-1.5"
              >
                <Icon
                  icon={IconCircleCheck}
                  size={14}
                  className="mt-0.5 shrink-0 text-(--color-text-brand)"
                />
                <p className="typo-body6 leading-relaxed text-(--color-text-secondary)">
                  {reason.title}
                </p>
              </li>
            ))}
          </ul>
          <button
            type="button"
            onClick={onOpenContext}
            className="ml-auto cursor-pointer typo-caption1 font-medium text-(--color-text-brand) hover:underline"
          >
            자세한 논의 보기
          </button>
        </GlassCard>
      </div>
    </section>
  );
};

export default OverviewTab;
