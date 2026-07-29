import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import type { DecisionContextMessage } from "@/types/decision";
import GlassCard from "./GlassCard";

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
            key={`${m.member_id}-${m.time}-${m.dialogue_content}`}
            className="flex w-full items-start gap-2"
          >
            {m.profile_image ? (
              <img
                src={m.profile_image}
                alt={m.member_name}
                className="size-7 shrink-0 rounded-full object-cover"
                onError={(e) => {
                  e.currentTarget.style.display = "none";
                  const fallback = e.currentTarget
                    .nextElementSibling as HTMLElement | null;
                  if (fallback) fallback.style.display = "inline-flex";
                }}
              />
            ) : null}
            <Icon
              icon={IconCircleUser}
              size={28}
              className="shrink-0 text-(--color-dark-100)"
              style={m.profile_image ? { display: "none" } : undefined}
            />
            <div className="flex min-w-0 flex-1 flex-col gap-1">
              <div className="flex items-center gap-1">
                <p className="typo-subtitle5 text-(--color-text-primary)">
                  {m.member_name}
                </p>
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
