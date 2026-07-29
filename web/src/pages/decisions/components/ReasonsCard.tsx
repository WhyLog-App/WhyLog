import IconNoteSearch from "@/assets/icons/file/ic_note_search.svg?react";
import IconCircleCheck from "@/assets/icons/warning/ic_circle_check.svg?react";
import { Icon } from "@/components/common/Icon";
import type { DecisionReasonItem } from "@/types/decision";
import GlassCard from "./GlassCard";

interface ReasonsCardProps {
  reasons: DecisionReasonItem[];
  count: number;
}

const ReasonsCard = ({ reasons, count }: ReasonsCardProps) => {
  return (
    <GlassCard className="h-51 gap-3 overflow-hidden px-4 py-5">
      <div className="flex items-center gap-1">
        <Icon
          icon={IconNoteSearch}
          size={16}
          className="text-(--color-text-primary)"
        />
        <p className="typo-body6 text-(--color-text-primary)">결정근거</p>
        <p className="typo-caption2 text-(--color-text-secondary)">({count})</p>
      </div>

      <ul className="flex flex-1 flex-col gap-1 overflow-y-auto">
        {reasons.map((reason) => (
          <li
            key={reason.reason_id}
            className="flex items-center gap-2 rounded-lg px-2 py-1.5"
          >
            <Icon
              icon={IconCircleCheck}
              size={14}
              className="shrink-0 text-(--color-text-brand)"
            />
            <p className="typo-body5 flex-1 text-(--color-text-primary)">
              {reason.title}
            </p>
          </li>
        ))}
      </ul>
    </GlassCard>
  );
};

export default ReasonsCard;
