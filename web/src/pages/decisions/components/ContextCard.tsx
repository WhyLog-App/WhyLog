import { Link } from "react-router-dom";
import GlassCard from "@/components/common/GlassCard";
import MemberAvatar from "@/components/common/MemberAvatar";
import { createMemberProfileRoute } from "@/constants/routes";
import type { DecisionContextMessage } from "@/types/decision";

interface ContextCardProps {
  messages: DecisionContextMessage[];
  className?: string;
}

const ContextCard = ({ messages, className = "" }: ContextCardProps) => {
  return (
    <GlassCard className={`gap-5 px-5 py-7 ${className}`}>
      <h2 className="typo-subtitle4 text-(--color-text-primary)">
        결정 원문 맥락
      </h2>

      <div className="flex flex-1 flex-col gap-4 overflow-y-auto">
        {messages.map((m) => (
          <div
            key={`${m.member_id ?? "anonymous"}-${m.time}-${m.dialogue_content}`}
            className="flex w-full items-start gap-2"
          >
            <MemberAvatar
              src={m.profile_image}
              alt={m.member_name ?? "알 수 없음"}
              size={28}
            />
            <div className="flex min-w-0 flex-1 flex-col gap-1">
              <div className="flex items-center gap-1">
                {m.member_id == null ? (
                  <span className="typo-subtitle5 text-(--color-text-primary)">
                    {m.member_name ?? "알 수 없음"}
                  </span>
                ) : (
                  <Link
                    to={createMemberProfileRoute(m.member_id)}
                    className="typo-subtitle5 text-(--color-text-primary) hover:text-(--color-text-secondary) focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--color-border-brand)"
                    aria-label={`${m.member_name ?? "알 수 없음"} 프로필 보기`}
                    title={`${m.member_name ?? "알 수 없음"} 프로필 보기`}
                  >
                    {m.member_name ?? "알 수 없음"}
                  </Link>
                )}
                <p className="typo-caption1 text-(--color-text-secondary)">
                  {m.time}
                </p>
              </div>
              <div
                className="bg-(--color-bg-surface) px-3 py-2"
                style={{
                  borderRadius: "2px 10px 10px 10px",
                }}
              >
                <p className="typo-body5 text-(--color-text-primary)">
                  {m.dialogue_content}
                </p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </GlassCard>
  );
};

export default ContextCard;
