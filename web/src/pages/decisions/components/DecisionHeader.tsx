import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
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

const DecisionHeader = ({ name, confidence, meta }: DecisionHeaderProps) => {
  const visibleAvatars = meta.participants.slice(0, 5);

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
          <div className="flex items-center pr-[3.5px]">
            {visibleAvatars.map((p) =>
              p.profile_image ? (
                <img
                  key={p.member_id}
                  src={p.profile_image}
                  alt={p.name}
                  className="-mr-[3.5px] size-3.5 shrink-0 rounded-full object-cover ring-1 ring-white"
                />
              ) : (
                <Icon
                  key={p.member_id}
                  icon={IconCircleUser}
                  size={14}
                  className="-mr-[3.5px] shrink-0 rounded-full bg-white text-(--color-dark-100) ring-1 ring-white"
                />
              ),
            )}
          </div>
          <span>{meta.participant_count}명 참여</span>
        </div>
      </div>
    </header>
  );
};

export default DecisionHeader;
