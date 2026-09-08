import IconChevronDownDuo from "@/assets/icons/arrow/ic_chevron_down_duo.svg?react";
import GlassCard from "@/components/common/GlassCard";
import { Icon } from "@/components/common/Icon";
import { TeamImage } from "@/components/common/TeamImage";
import type { ParticipatingProjectSummary } from "@/types/member";
import { formatDateTime, formatDurationSeconds } from "@/utils/date";

interface ProjectMetricProps {
  label: string;
  value: string;
}

const ProjectMetric = ({ label, value }: ProjectMetricProps) => (
  <div className="min-w-0 rounded-2xl bg-white/50 px-4 py-3">
    <dt
      className="typo-caption1 truncate text-(--color-text-tertiary)"
      title={label}
    >
      {label}
    </dt>
    <dd
      className="typo-subtitle5 truncate text-(--color-text-primary)"
      title={value}
    >
      {value}
    </dd>
  </div>
);

interface ProjectListProps {
  projects: ParticipatingProjectSummary[];
  isLoading?: boolean;
  isError?: boolean;
  isNextPageError?: boolean;
  hasNextPage?: boolean;
  isFetchingNextPage?: boolean;
  onLoadMore?: () => void;
  onRetry?: () => void;
}

export const ProjectList = ({
  projects,
  isLoading = false,
  isError = false,
  isNextPageError = false,
  hasNextPage = false,
  isFetchingNextPage = false,
  onLoadMore,
  onRetry,
}: ProjectListProps) => {
  if (isLoading && projects.length === 0) {
    return (
      <GlassCard className="px-5 py-10 text-center">
        <p className="typo-body4 text-(--color-text-secondary)">
          참여 프로젝트를 불러오는 중입니다.
        </p>
      </GlassCard>
    );
  }

  if (isError && projects.length === 0) {
    return (
      <GlassCard className="items-center gap-4 px-5 py-10 text-center">
        <p className="typo-body4 text-(--color-text-secondary)">
          참여 프로젝트를 불러오지 못했습니다.
        </p>
        {onRetry && (
          <button
            type="button"
            onClick={onRetry}
            className="typo-button-md cursor-pointer rounded-xl bg-(--color-action-primary) px-5 py-2.5 text-(--color-text-inverse) transition-opacity hover:opacity-90"
          >
            다시 시도
          </button>
        )}
      </GlassCard>
    );
  }

  if (projects.length === 0) {
    return (
      <GlassCard className="px-5 py-10 text-center">
        <p className="typo-body4 text-(--color-text-secondary)">
          참여 중인 프로젝트가 없습니다.
        </p>
      </GlassCard>
    );
  }

  return (
    <div className="flex min-w-0 flex-col gap-4">
      <div className="grid min-w-0 grid-cols-1 gap-4 lg:grid-cols-2">
        {projects.map((project) => (
          <GlassCard
            key={project.project_id}
            className="min-w-0 gap-5 p-5 sm:p-6"
          >
            <div className="flex min-w-0 items-center gap-3">
              <TeamImage
                src={project.image}
                alt={`${project.name} 이미지`}
                size={42}
                className="rounded-lg ring-2 ring-white/60"
              />
              <div className="min-w-0">
                <h3
                  className="typo-subtitle4 truncate text-(--color-text-primary)"
                  title={project.name}
                >
                  {project.name}
                </h3>
                <p
                  className="typo-body6 truncate text-(--color-text-tertiary)"
                  title={`마지막 동기화 ${formatDateTime(project.last_synced_at)}`}
                >
                  마지막 동기화 {formatDateTime(project.last_synced_at)}
                </p>
              </div>
            </div>
            <dl className="grid min-w-0 grid-cols-1 gap-3 sm:grid-cols-3">
              <ProjectMetric
                label="참여 회의 횟수"
                value={`${project.member_completed_meeting_count}회`}
              />
              <ProjectMetric
                label="참여 회의 시간"
                value={formatDurationSeconds(
                  project.member_completed_meeting_duration_seconds,
                )}
              />
              <ProjectMetric
                label="전체 커밋 수"
                value={`${project.project_stored_commit_count}개`}
              />
            </dl>
          </GlassCard>
        ))}
      </div>

      {isNextPageError && onRetry && (
        <div className="flex flex-col items-center gap-2 text-center">
          <p className="typo-caption1 text-(--color-text-tertiary)">
            추가 프로젝트를 불러오지 못했습니다.
          </p>
          <button
            type="button"
            onClick={onRetry}
            disabled={isFetchingNextPage}
            className="typo-body5 cursor-pointer rounded-full px-4 py-1.5 text-(--color-text-secondary) transition-colors hover:text-(--color-text-primary) disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isFetchingNextPage ? "불러오는 중..." : "다시 시도"}
          </button>
        </div>
      )}

      {hasNextPage && !isNextPageError && onLoadMore && (
        <div className="flex justify-center">
          <button
            type="button"
            onClick={onLoadMore}
            disabled={isFetchingNextPage}
            className="flex cursor-pointer items-center gap-2 rounded-full px-4 py-1.5 text-(--color-text-secondary) transition-colors hover:text-(--color-text-primary) disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Icon
              icon={IconChevronDownDuo}
              size={16}
              className="text-current"
            />
            <span className="typo-body5 font-medium">
              {isFetchingNextPage ? "불러오는 중..." : "더 불러오기"}
            </span>
          </button>
        </div>
      )}
    </div>
  );
};
