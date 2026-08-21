import { useEffect, useState } from "react";
import IconCircleUser from "@/assets/icons/user/ic_circle_user.svg?react";
import { Icon } from "@/components/common/Icon";
import type {
  DecisionContextMessage,
  DecisionReasonItem,
} from "@/types/decision";
import GlassCard from "./GlassCard";

interface OriginalContextTabProps {
  messages: DecisionContextMessage[];
  reasons: DecisionReasonItem[];
}

const ContextMessage = ({
  message,
  selected,
  onSelect,
}: {
  message: DecisionContextMessage;
  selected: boolean;
  onSelect: () => void;
}) => (
  <button
    type="button"
    onClick={onSelect}
    className={`flex w-full gap-2 rounded-xl p-3 text-left transition-colors ${
      selected
        ? "border-[1.5px] border-(--color-border-brand) bg-(--color-bg-brand-subtle)"
        : "border-[1.5px] border-transparent hover:bg-(--color-bg-subtle)"
    }`}
  >
    {message.profile_image ? (
      <img
        src={message.profile_image}
        alt=""
        className="size-7 shrink-0 rounded-full object-cover"
      />
    ) : (
      <Icon
        icon={IconCircleUser}
        size={28}
        className="shrink-0 text-(--color-dark-100)"
      />
    )}
    <span className="flex min-w-0 flex-1 flex-col gap-1">
      <span className="flex items-center gap-1">
        <span className="typo-body6 font-medium text-(--color-text-primary)">
          {message.member_name}
        </span>
        <span className="typo-caption1 text-(--color-text-secondary)">
          {message.time}
        </span>
      </span>
      <span className="rounded-bl-lg rounded-br-lg rounded-tl-sm rounded-tr-lg bg-(--color-bg-surface) px-3 py-2 typo-body6 leading-relaxed text-(--color-text-primary)">
        {message.dialogue_content}
      </span>
    </span>
  </button>
);

const OriginalContextTab = ({ messages, reasons }: OriginalContextTabProps) => {
  const [selectedIndex, setSelectedIndex] = useState(0);
  // biome-ignore lint/correctness/useExhaustiveDependencies: messages 참조 변경을 트리거로 사용
  useEffect(() => {
    setSelectedIndex(0);
  }, [messages]);
  const safeIndex = selectedIndex < messages.length ? selectedIndex : 0;
  const selectedMessage = messages[safeIndex];
  const relatedMessages = messages
    .map((message, index) => ({ message, index }))
    .filter(({ index }) => index !== safeIndex);
  const primaryReason = reasons[0]?.title ?? "결정 핵심 근거";

  if (!selectedMessage) {
    return (
      <GlassCard className="items-center justify-center px-7 py-20">
        <p className="typo-body6 text-(--color-text-tertiary)">
          표시할 원문 맥락이 없습니다.
        </p>
      </GlassCard>
    );
  }

  return (
    <section className="grid min-h-0 grid-cols-[minmax(17rem,0.55fr)_minmax(0,1fr)] gap-6">
      <GlassCard className="min-h-150 gap-4 p-5">
        <h2 className="typo-body5 font-bold text-(--color-text-primary)">
          맥락 목록
        </h2>
        <div className="application-scroll flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto pr-1">
          {messages.map((message, index) => (
            <ContextMessage
              key={`${message.member_id}-${message.time}-${message.dialogue_content}`}
              message={message}
              selected={safeIndex === index}
              onSelect={() => setSelectedIndex(index)}
            />
          ))}
        </div>
      </GlassCard>

      <div className="flex min-w-0 flex-col gap-5">
        <GlassCard className="gap-4 px-7 py-6">
          <p className="typo-caption1 text-(--color-text-tertiary)">
            {selectedMessage.time} · {selectedMessage.member_name}
          </p>
          <h2 className="typo-title3 text-(--color-text-primary)">
            {selectedMessage.content || primaryReason}
          </h2>
          <blockquote className="rounded-lg bg-(--color-bg-subtle) px-5 py-4 typo-body6 leading-relaxed text-(--color-text-primary)">
            “{selectedMessage.dialogue_content}”
          </blockquote>
        </GlassCard>

        <GlassCard className="gap-3 px-7 py-5">
          <h2 className="typo-body5 font-bold text-(--color-text-primary)">
            관련 맥락
          </h2>
          {relatedMessages.map(({ message, index }) => (
            <button
              key={`${message.member_id}-${message.time}-${message.dialogue_content}`}
              type="button"
              onClick={() => setSelectedIndex(index)}
              className="flex min-w-0 gap-2.5 text-left"
            >
              <span className="flex w-20 shrink-0 flex-col typo-caption1 text-(--color-text-brand)">
                <span>{message.time}</span>
                <span className="font-medium">{message.member_name}</span>
              </span>
              <span className="flex min-w-0 flex-1 flex-col gap-0.5">
                <span className="line-clamp-2 typo-caption2 text-(--color-text-tertiary)">
                  {message.content || message.dialogue_content}
                </span>
              </span>
            </button>
          ))}
        </GlassCard>

        <div className="flex flex-col gap-2 rounded-xl bg-(--color-purple-50) px-7 py-4 text-(--color-purple-700)">
          <p className="typo-caption1 font-bold">✨ 요약 (AI 생성)</p>
          <p className="typo-caption1 leading-relaxed">
            선택한 발언은 {primaryReason}와 관련된 결정의 맥락을 보여줍니다.
          </p>
        </div>
      </div>
    </section>
  );
};

export default OriginalContextTab;
