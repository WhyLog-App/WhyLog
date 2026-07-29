import { useEffect, useMemo, useRef } from "react";
import IconChatConversation from "@/assets/icons/media/ic_chat_conversation.svg?react";
import { Icon } from "@/components/common/Icon";
import type { InterimEntry, TranscriptEntry } from "../types";
import { getMemberColor } from "../utils/memberColor";

interface LiveTranscriptPanelProps {
  transcripts: TranscriptEntry[];
  interimByMember: Record<string, InterimEntry>;
  isSupported?: boolean;
}

const formatTime = (iso: string) => {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const LiveTranscriptPanel = ({
  transcripts,
  interimByMember,
  isSupported = true,
}: LiveTranscriptPanelProps) => {
  const scrollRef = useRef<HTMLDivElement>(null);
  const isStickToBottomRef = useRef(true);

  const interimEntries = useMemo(
    () => Object.entries(interimByMember),
    [interimByMember],
  );

  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const el = e.currentTarget;
    const distanceFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight;
    isStickToBottomRef.current = distanceFromBottom <= 24;
  };

  // biome-ignore lint/correctness/useExhaustiveDependencies: Need to scroll when transcripts or interimEntries change
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    if (!isStickToBottomRef.current) return;
    el.scrollTop = el.scrollHeight;
  }, [transcripts, interimEntries]);

  const isEmpty = transcripts.length === 0 && interimEntries.length === 0;

  return (
    <aside className="flex h-[40vh] w-full shrink-0 flex-col gap-4 overflow-hidden rounded-2xl border border-white bg-white/50 px-5 py-5 backdrop-blur-md lg:h-full lg:w-90">
      <div className="flex items-center gap-2">
        <Icon
          icon={IconChatConversation}
          size={20}
          className="text-text-brand-darker"
        />
        <p className="typo-body6 text-(--color-text-primary)">
          실시간 음성인식
        </p>
      </div>

      {!isSupported && (
        <div className="rounded-md bg-yellow-50 px-3 py-2">
          <p className="typo-caption1 text-yellow-800">
            이 브라우저는 실시간 자막을 지원하지 않습니다. (Chrome/Edge 권장)
          </p>
        </div>
      )}

      <div
        ref={scrollRef}
        onScroll={handleScroll}
        className="flex flex-1 flex-col gap-4 overflow-y-auto"
      >
        {isEmpty && (
          <p className="typo-body6 text-(--color-text-tertiary)">
            아직 인식된 발화가 없습니다.
          </p>
        )}

        {transcripts.map((item) => (
          <div key={item.id} className="flex flex-col gap-1">
            <div className="flex items-center justify-between">
              <span
                className="typo-subtitle5"
                style={{ color: getMemberColor(item.memberId, item.fromName) }}
              >
                {item.fromName}
              </span>
              <span className="typo-caption1 text-(--color-text-tertiary)">
                {formatTime(item.timestamp)}
              </span>
            </div>
            <p className="typo-body6 text-(--color-text-secondary)">
              {item.text}
            </p>
          </div>
        ))}

        {interimEntries.map(([key, entry]) => (
          <div
            key={`interim-${key}`}
            className="flex flex-col gap-1 opacity-60"
          >
            <div className="flex items-center justify-between">
              <span
                className="typo-subtitle5"
                style={{ color: getMemberColor(entry.memberId, key) }}
              >
                {entry.fromName}
              </span>
              <span className="typo-caption1 text-(--color-text-tertiary)">
                {formatTime(entry.timestamp)}
              </span>
            </div>
            <p className="typo-body6 text-(--color-text-secondary) italic">
              {entry.text}
            </p>
          </div>
        ))}
      </div>
    </aside>
  );
};

export default LiveTranscriptPanel;
