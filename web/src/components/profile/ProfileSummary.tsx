import GlassCard from "@/components/common/GlassCard";
import MemberAvatar from "@/components/common/MemberAvatar";

interface ProfileSummaryProps {
  name: string;
  email: string;
  profileImage?: string | null;
  projectCount?: number;
}

export const ProfileSummary = ({
  name,
  email,
  profileImage,
  projectCount,
}: ProfileSummaryProps) => {
  return (
    <GlassCard className="min-w-0 gap-6 p-6 sm:flex-row sm:items-center sm:justify-between sm:p-8">
      <div className="flex min-w-0 items-center gap-5">
        <MemberAvatar
          src={profileImage}
          alt={`${name} 프로필 이미지`}
          size={80}
        />
        <div className="flex h-20 min-w-0 flex-col justify-center gap-1">
          <h1
            className="typo-h4 m-0 truncate text-(--color-text-primary)"
            title={name}
          >
            {name}
          </h1>
          {email && (
            <p
              className="typo-body5 m-0 truncate text-(--color-text-secondary)"
              title={email}
            >
              {email}
            </p>
          )}
        </div>
      </div>
      {projectCount != null && (
        <dl className="sm:min-w-32 sm:text-right">
          <div>
            <dt className="typo-body6 text-(--color-text-tertiary)">
              참여 프로젝트
            </dt>
            <dd className="typo-subtitle2 text-(--color-text-brand-darker)">
              {projectCount}개
            </dd>
          </div>
        </dl>
      )}
    </GlassCard>
  );
};
