import { Link } from "react-router-dom";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import { createMemberProfileRoute } from "@/constants/routes";
import type { DecisionConfidence, DecisionMeetingMeta } from "@/types/decision";
import ConfidenceBadge from "./ConfidenceBadge";

interface DecisionHeaderProps {
  name: string;
  confidence: DecisionConfidence;
  meta: DecisionMeetingMeta;
}

const Dot = () => (
  <span className="typo-body6 text-(--color-text-secondary)">·</span>
);

const ParticipantAvatar = ({
  name,
  profileImage,
}: {
  name: string;
  profileImage: string | null;
}) =>
  profileImage ? (
    <img
      src={profileImage}
      alt={name}
      className="size-3.5 rounded-full object-cover ring-1 ring-white"
    />
  ) : (
    <Icon
      icon={IconCircleUser}
      size={14}
      className="rounded-full bg-white text-(--color-dark-100) ring-1 ring-white"
    />
  );

const DecisionHeader = ({ name, confidence, meta }: DecisionHeaderProps) => {
  const visibleAvatars = meta.participants.slice(0, 5);
  const participantKeyCounts = new Map<string, number>();

  return (
    <header className="flex w-full flex-col gap-3">
      <div className="flex items-center gap-3">
        <h1 className="typo-h4 text-(--color-text-primary)">{name}</h1>
        <ConfidenceBadge score={confidence.score} />
      </div>

      <div className="flex w-full flex-wrap items-center gap-x-2 gap-y-1 typo-body6 text-(--color-text-secondary)">
        <span>{meta.meeting_name}</span>
        <Dot />
        <span>{meta.meeting_date}</span>
        <Dot />
        <span>{meta.duration_label}</span>
        <Dot />
        <div className="flex items-center gap-1">
          <div className="flex items-center gap-0.5">
            {visibleAvatars.map((participant) => {
              const baseKey = String(
                participant.member_id ?? `anonymous-${participant.name}`,
              );
              const occurrence = participantKeyCounts.get(baseKey) ?? 0;
              participantKeyCounts.set(baseKey, occurrence + 1);
              const key = `${baseKey}-${occurrence}`;
              const className =
                "flex size-6 shrink-0 items-center justify-center rounded-full";
              const avatar = (
                <ParticipantAvatar
                  key={key}
                  name={participant.name}
                  profileImage={participant.profile_image}
                />
              );

              return participant.member_id == null ? (
                <span key={key} className={className}>
                  {avatar}
                </span>
              ) : (
                <Link
                  key={key}
                  to={createMemberProfileRoute(participant.member_id)}
                  className={`${className} focus-visible:z-10 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--color-border-brand)`}
                  aria-label={`${participant.name} 프로필 보기`}
                  title={`${participant.name} 프로필 보기`}
                >
                  {avatar}
                </Link>
              );
            })}
          </div>
          <span>{meta.participant_count}명 참여</span>
        </div>
      </div>
    </header>
  );
};

export default DecisionHeader;
