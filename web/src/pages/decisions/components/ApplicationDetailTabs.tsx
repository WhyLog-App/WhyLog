export type ApplicationDetailTab = "overview" | "relation" | "context" | "code";

const TABS: { id: ApplicationDetailTab; label: string }[] = [
  { id: "overview", label: "개요" },
  { id: "relation", label: "관계도" },
  { id: "context", label: "원문 맥락" },
  { id: "code", label: "코드 반영" },
];

interface ApplicationDetailTabsProps {
  activeTab: ApplicationDetailTab;
  onTabChange?: (tab: ApplicationDetailTab) => void;
}

const ApplicationDetailTabs = ({
  activeTab,
  onTabChange,
}: ApplicationDetailTabsProps) => (
  <nav
    aria-label="적용사항 상세 탭"
    className="flex items-center gap-7 border-b border-(--color-border-default)"
  >
    {TABS.map((tab) => {
      const active = tab.id === activeTab;
      return (
        <button
          key={tab.id}
          type="button"
          aria-current={active ? "page" : undefined}
          onClick={() => onTabChange?.(tab.id)}
          className={`-mb-px cursor-pointer border-b-2 px-0.5 pb-2 typo-button-md transition-colors ${
            active
              ? "border-(--color-text-brand) text-(--color-text-brand)"
              : "border-transparent text-(--color-text-secondary) hover:border-(--color-bg-brand-subtle) hover:text-(--color-text-brand)"
          }`}
        >
          {tab.label}
        </button>
      );
    })}
  </nav>
);

export default ApplicationDetailTabs;
