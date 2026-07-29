import type { CompletedMeetingPanels } from "../../types/completed";
import InfoPanelCard from "./InfoPanelCard";

interface CompletedInfoPanelsProps {
  panels: CompletedMeetingPanels;
  isAnalyzing?: boolean;
}

const ANALYZING_PLACEHOLDER = ["분석 중입니다..."];

const CompletedInfoPanels = ({
  panels,
  isAnalyzing = false,
}: CompletedInfoPanelsProps) => {
  const sections: { title: string; items: string[] }[] = [
    { title: "회의 기본 정보", items: panels.basicInfo },
    { title: "논의 주제", items: panels.topics },
    { title: "핵심 맥락", items: panels.context },
    { title: "최종 결정", items: panels.finalDecisions },
    { title: "결정 근거", items: panels.rationale },
    { title: "기타 언급", items: panels.misc },
  ];

  const visible = isAnalyzing
    ? sections.map((s) => ({
        ...s,
        items: s.items.length === 0 ? ANALYZING_PLACEHOLDER : s.items,
      }))
    : sections.filter((s) => s.items.length > 0);

  if (visible.length === 0) return null;

  return (
    <aside className="flex w-full shrink-0 flex-col gap-4 lg:h-full lg:w-80 lg:overflow-y-auto">
      {visible.map((s) => (
        <InfoPanelCard key={s.title} title={s.title} items={s.items} />
      ))}
    </aside>
  );
};

export default CompletedInfoPanels;
