import { Link } from "react-router-dom";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import { createMemberProfileRoute } from "@/constants/routes";
import type { CompletedTranscriptItem } from "../types/completed";

interface CompletedTranscriptProps {
  items: CompletedTranscriptItem[];
}

const CompletedTranscript = ({ items }: CompletedTranscriptProps) => {
  return (
    <div className="flex flex-1 flex-col gap-5 overflow-y-auto pr-2">
      {items.map((item) => (
        <div key={item.id} className="flex items-start gap-3">
          <span className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center overflow-hidden rounded-full bg-(--color-bg-subtle)">
            {item.profile_image ? (
              <img
                src={item.profile_image}
                alt={item.name}
                className="h-full w-full object-cover"
              />
            ) : (
              <Icon
                icon={IconCircleUser}
                size={24}
                className="text-(--color-dark-100)"
              />
            )}
          </span>
          <div className="flex flex-1 flex-col gap-1">
            <div className="flex items-baseline gap-2">
              {item.profile_member_id == null ? (
                <span className="typo-subtitle5 text-(--color-text-primary)">
                  {item.name}
                </span>
              ) : (
                <Link
                  to={createMemberProfileRoute(item.profile_member_id)}
                  className="typo-subtitle5 text-(--color-text-primary) hover:text-(--color-text-secondary) focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-(--color-border-brand)"
                  aria-label={`${item.name} 프로필 보기`}
                  title={`${item.name} 프로필 보기`}
                >
                  {item.name}
                </Link>
              )}
              <span className="typo-caption1 text-(--color-text-tertiary)">
                {item.time}
              </span>
            </div>
            <p className="typo-body6 wrap-break-word text-(--color-text-secondary)">
              {item.text}
            </p>
          </div>
        </div>
      ))}
    </div>
  );
};

export default CompletedTranscript;
