import GlassCard from "@/components/common/GlassCard";
import { ProjectList } from "@/components/profile/ProjectList";
import { useMemberProjects } from "@/hooks/useMemberProjects";
import type { MyInfo } from "@/types/member";
import { formatDateTime, formatDurationSeconds } from "@/utils/date";

interface ActivityTabProps {
  info: MyInfo;
}

export const ActivityTab = ({ info }: ActivityTabProps) => {
  const {
    projects,
    isLoading: isProjectsLoading,
    isError: isProjectsError,
    hasNextPage,
    isFetchingNextPage,
    isFetchNextPageError,
    fetchNextPage,
    refetch: refetchProjects,
  } = useMemberProjects(info.member_id);

  const retryProjects = () => {
    if (isFetchNextPageError) {
      void fetchNextPage();
      return;
    }
    void refetchProjects();
  };

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4">
        <div>
          <h2 className="typo-h5 text-(--color-text-primary)">참여 프로젝트</h2>
          <p className="typo-body6 text-(--color-text-tertiary)">
            참여 중인 프로젝트와 저장된 활동 지표입니다.
          </p>
        </div>
        <ProjectList
          projects={projects}
          isLoading={isProjectsLoading}
          isError={isProjectsError}
          isNextPageError={isFetchNextPageError}
          hasNextPage={hasNextPage}
          isFetchingNextPage={isFetchingNextPage}
          onLoadMore={() => {
            void fetchNextPage();
          }}
          onRetry={retryProjects}
        />
      </section>

      <section className="grid min-w-0 gap-5 lg:grid-cols-2">
        <GlassCard className="min-w-0 gap-4 p-5 sm:p-6">
          <div>
            <h2 className="typo-h5 text-(--color-text-primary)">
              최근 완료 회의
            </h2>
            <p className="typo-body6 text-(--color-text-tertiary)">
              내가 참여한 최근 완료 회의입니다.
            </p>
          </div>
          {info.recent_meetings.length === 0 ? (
            <p className="rounded-2xl bg-white/50 py-8 text-center text-(--color-text-secondary)">
              최근 완료 회의가 없습니다.
            </p>
          ) : (
            <ul className="flex min-w-0 flex-col gap-6">
              {info.recent_meetings.map((meeting) => (
                <li
                  key={meeting.meeting_id}
                  className="flex min-w-0 flex-col gap-1"
                >
                  <span
                    className="typo-subtitle5 truncate text-(--color-text-primary)"
                    title={meeting.name}
                  >
                    {meeting.name}
                  </span>
                  <span
                    className="typo-caption1 truncate text-(--color-text-tertiary)"
                    title={`${meeting.project_name} · ${formatDateTime(meeting.ended_at)} · ${formatDurationSeconds(meeting.duration_seconds)}`}
                  >
                    {meeting.project_name} · {formatDateTime(meeting.ended_at)}{" "}
                    · {formatDurationSeconds(meeting.duration_seconds)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </GlassCard>

        <GlassCard className="min-w-0 gap-4 p-5 sm:p-6">
          <div>
            <h2 className="typo-h5 text-(--color-text-primary)">최근 결정</h2>
            <p className="typo-body6 text-(--color-text-tertiary)">
              회의에서 정리된 최근 결정입니다.
            </p>
          </div>
          {info.recent_decisions.length === 0 ? (
            <p className="rounded-2xl bg-white/50 py-8 text-center text-(--color-text-secondary)">
              최근 결정이 없습니다.
            </p>
          ) : (
            <ul className="flex min-w-0 flex-col gap-6">
              {info.recent_decisions.map((decision) => (
                <li
                  key={decision.decision_id}
                  className="flex min-w-0 flex-col gap-1"
                >
                  <span
                    className="typo-subtitle5 truncate text-(--color-text-primary)"
                    title={decision.name}
                  >
                    {decision.name}
                  </span>
                  <span
                    className="typo-caption1 truncate text-(--color-text-tertiary)"
                    title={`${decision.project_name} · ${formatDateTime(decision.created_at)}`}
                  >
                    {decision.project_name} ·{" "}
                    {formatDateTime(decision.created_at)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </GlassCard>
      </section>
    </div>
  );
};
