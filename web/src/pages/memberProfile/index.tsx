import { isAxiosError } from "axios";
import { useParams } from "react-router-dom";
import IconLock from "@/assets/icons/interface/ic_lock.svg?react";
import GlassCard from "@/components/common/GlassCard";
import { Icon } from "@/components/common/Icon";
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { ProfileSummary } from "@/components/profile/ProfileSummary";
import { ProjectList } from "@/components/profile/ProjectList";
import { useMemberProjects } from "@/hooks/useMemberProjects";
import type { ApiResponse } from "@/types/auth";
import type { ProfileView } from "@/types/member";
import { parseRouteId } from "@/utils/parseRouteId";
import { useMemberProfile } from "./hooks/useMemberProfile";

const getProfileErrorCopy = (
  memberId: number | null,
  error: unknown,
): { message: string; canRetry: boolean } => {
  if (memberId == null) {
    return { message: "잘못된 멤버 프로필 주소입니다.", canRetry: false };
  }

  if (isAxiosError<ApiResponse<unknown>>(error)) {
    if (error.response?.status === 404) {
      return { message: "멤버 프로필을 찾을 수 없습니다.", canRetry: false };
    }
    return {
      message:
        error.response?.data?.message ??
        "멤버 프로필을 불러오지 못했습니다. 다시 시도해 주세요.",
      canRetry: true,
    };
  }

  return {
    message: "멤버 프로필을 불러오지 못했습니다. 다시 시도해 주세요.",
    canRetry: true,
  };
};

const getProjectDescription = (profileView: ProfileView) => {
  switch (profileView) {
    case "PRIVATE":
      return "프로필 공개 설정에 따라 활동 정보가 비공개입니다.";
    case "WITHDRAWN":
      return "탈퇴한 계정의 참여 프로젝트는 공개되지 않습니다.";
    default:
      return "이 멤버가 현재 참여 중인 프로젝트입니다.";
  }
};

const PrivateProfileState = () => (
  <GlassCard className="items-center gap-4 px-6 py-12 text-center sm:py-16">
    <div className="flex size-14 items-center justify-center rounded-2xl border border-(--color-border-primary) text-(--color-text-tertiary)">
      <Icon icon={IconLock} size={28} />
    </div>
    <div className="flex flex-col gap-2">
      <h3 className="typo-subtitle2 text-(--color-text-primary)">
        비공개 프로필입니다
      </h3>
      <p className="typo-body5 text-(--color-text-secondary)">
        이 멤버는 프로젝트 활동을 공개하지 않았습니다.
      </p>
    </div>
  </GlassCard>
);

const WithdrawnProfileState = () => (
  <GlassCard className="items-center gap-3 px-6 py-12 text-center sm:py-16">
    <h3 className="typo-subtitle2 text-(--color-text-primary)">
      탈퇴한 사용자입니다
    </h3>
    <p className="typo-body5 text-(--color-text-secondary)">
      계정 정보는 삭제되었지만 기존 회의와 결정 이력은 유지됩니다.
    </p>
  </GlassCard>
);

function MemberProfilePage() {
  const { memberId } = useParams<{ memberId: string }>();
  const parsedMemberId = parseRouteId(memberId);
  const { data, isLoading, isError, error, refetch } =
    useMemberProfile(parsedMemberId);
  const isFullProfile = data?.profile_view === "FULL";
  const {
    projects,
    isLoading: isProjectsLoading,
    isError: isProjectsError,
    hasNextPage,
    isFetchingNextPage,
    isFetchNextPageError,
    fetchNextPage,
    refetch: refetchProjects,
  } = useMemberProjects(parsedMemberId, { enabled: isFullProfile });

  if (isLoading) return <LoadingSpinner />;

  if (parsedMemberId == null || isError || !data) {
    const profileErrorCopy = getProfileErrorCopy(parsedMemberId, error);
    return (
      <div className="flex min-h-full flex-col items-center justify-center gap-4 py-20 text-center">
        <p className="typo-body4 text-(--color-text-secondary)">
          {profileErrorCopy.message}
        </p>
        {profileErrorCopy.canRetry && (
          <button
            type="button"
            onClick={() => refetch()}
            className="typo-button-md cursor-pointer rounded-xl bg-(--color-action-primary) px-5 py-2.5 text-(--color-text-inverse) transition-opacity hover:opacity-90"
          >
            다시 시도
          </button>
        )}
      </div>
    );
  }

  const isPrivate = data.profile_view === "PRIVATE";
  const isWithdrawn = data.profile_view === "WITHDRAWN";
  const projectCount =
    data.profile_view === "FULL" ? data.participating_project_count : undefined;
  const retryProjects = () => {
    if (isFetchNextPageError) {
      void fetchNextPage();
      return;
    }
    void refetchProjects();
  };

  return (
    <div className="mx-auto flex min-h-full w-full max-w-260 flex-col gap-8 py-10">
      <ProfileSummary
        name={data.name}
        email={data.email}
        profileImage={data.profile_image}
        projectCount={projectCount}
      />
      <section className="flex flex-col gap-3">
        <div>
          <h2 className="typo-h5 text-(--color-text-primary)">참여 프로젝트</h2>
          <p className="typo-body6 text-(--color-text-tertiary)">
            {getProjectDescription(data.profile_view)}
          </p>
        </div>
        {isWithdrawn ? (
          <WithdrawnProfileState />
        ) : isPrivate ? (
          <PrivateProfileState />
        ) : (
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
        )}
      </section>
    </div>
  );
}

export default MemberProfilePage;
