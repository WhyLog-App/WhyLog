import IconClock from "@/assets/icons/media/ic_clock.svg?react";
import { Icon } from "@/components/common/Icon";
import type { DecisionTimelineItem } from "@/types/decision";
import GlassCard from "./GlassCard";

interface TimelineCardProps {
  items: DecisionTimelineItem[];
}

const TimelineCard = ({ items }: TimelineCardProps) => {
  return (
    <GlassCard className="h-51 gap-3 overflow-hidden px-4 py-5">
      <div className="flex items-center gap-1">
        <Icon
          icon={IconClock}
          size={16}
          className="text-(--color-text-primary)"
        />
        <p className="typo-body6 text-(--color-text-primary)">타임라인</p>
      </div>

      <div className="relative flex flex-1 flex-col gap-2 overflow-y-auto">
        {/* 세로 연결선 */}
        {items.length > 1 && (
          <div
            aria-hidden="true"
            className="absolute top-4 bottom-4 left-4 w-px bg-(--color-border-default)"
          />
        )}
        {items.map((item) => (
          <div
            key={`${item.time}-${item.step}-${item.content}`}
            className="relative flex items-center gap-2 rounded-lg px-3 py-1.5"
          >
            <span
              aria-hidden="true"
              className="z-10 inline-block size-2 shrink-0 rounded-full bg-(--color-text-brand) ring-2 ring-white"
            />
            <p className="typo-caption2 shrink-0 text-(--color-text-secondary)">
              {item.time}
            </p>
            <p className="typo-body5 flex-1 wrap-break-word whitespace-pre-wrap text-(--color-text-primary)">
              {item.content}
            </p>
          </div>
        ))}
      </div>
    </GlassCard>
  );
};

export default TimelineCard;
