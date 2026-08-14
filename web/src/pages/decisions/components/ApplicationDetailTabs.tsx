export type ApplicationDetailTab = "overview" | "relation" | "context" | "code";

const TABS: { id: ApplicationDetailTab; label: string }[] = [
  { id: "overview", label: "개요" },
  { id: "relation", label: "관계도" },
  { id: "context", label: "원문 맥락" },
  { id: "code", label: "코드 반영" },
];

interface ApplicationDetailTabsProps {
  activeTab: ApplicationDetailTab;
}

const ApplicationDetailTabs = ({ activeTab }: ApplicationDetailTabsProps) => (
  <nav
    aria-label="적용사항 상세 탭"
    className="flex items-center gap-7 border-b border-(--color-border-default)"
  >
    {TABS.map((tab) => {
      const active = tab.id === activeTab;
      return (
        <span
          key={tab.id}
          className={`-mb-px border-b-2 px-0.5 pb-2 typo-button-md ${
            active
              ? "border-(--color-text-brand) text-(--color-text-brand)"
              : "border-transparent text-(--color-text-secondary)"
          }`}
        >
          {tab.label}
        </span>
      );
    })}
  </nav>
);

export default ApplicationDetailTabs;
