import type { KeyboardEvent } from "react";
import { useRef, useState } from "react";
import LoadingSpinner from "@/components/common/LoadingSpinner";
import { ProfileSummary } from "@/components/profile/ProfileSummary";
import { useMyInfo } from "@/hooks/useMyInfo";
import { AccountTab } from "./components/AccountTab";
import { ActivityTab } from "./components/ActivityTab";

type MyPageTab = "activity" | "account";

const MYPAGE_TAB_STORAGE_KEY = "whylog:mypage:active-tab";

const getInitialTab = (): MyPageTab => {
  try {
    return sessionStorage.getItem(MYPAGE_TAB_STORAGE_KEY) === "account"
      ? "account"
      : "activity";
  } catch (error) {
    console.error("마이페이지 탭 상태 조회 실패:", error);
    return "activity";
  }
};

const tabs: Array<{ id: MyPageTab; label: string }> = [
  { id: "activity", label: "활동" },
  { id: "account", label: "계정 관리" },
];

function MyPage() {
  const [activeTab, setActiveTab] = useState<MyPageTab>(getInitialTab);
  const tabRefs = useRef<Record<MyPageTab, HTMLButtonElement | null>>({
    activity: null,
    account: null,
  });
  const { data, isLoading, isError, refetch } = useMyInfo();

  const handleTabChange = (tab: MyPageTab) => {
    setActiveTab(tab);
    try {
      sessionStorage.setItem(MYPAGE_TAB_STORAGE_KEY, tab);
    } catch (error) {
      console.error("마이페이지 탭 상태 저장 실패:", error);
    }
  };

  const handleTabKeyDown = (
    event: KeyboardEvent<HTMLButtonElement>,
    currentIndex: number,
  ) => {
    const keyOffset = event.key === "ArrowRight" ? 1 : -1;
    let nextIndex: number | null = null;

    if (event.key === "ArrowRight" || event.key === "ArrowLeft") {
      nextIndex = (currentIndex + keyOffset + tabs.length) % tabs.length;
    } else if (event.key === "Home") {
      nextIndex = 0;
    } else if (event.key === "End") {
      nextIndex = tabs.length - 1;
    }

    if (nextIndex == null) return;
    event.preventDefault();
    const nextTab = tabs[nextIndex].id;
    handleTabChange(nextTab);
    tabRefs.current[nextTab]?.focus();
  };

  if (isLoading) return <LoadingSpinner />;

  if (isError || !data) {
    return (
      <div className="flex min-h-full flex-col items-center justify-center gap-4 py-20 text-center">
        <p className="typo-body4 text-(--color-text-secondary)">
          마이페이지를 불러오지 못했습니다.
        </p>
        <button
          type="button"
          onClick={() => refetch()}
          className="typo-button-md cursor-pointer rounded-xl bg-(--color-action-primary) px-5 py-2.5 text-(--color-text-inverse) transition-opacity hover:opacity-90"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto flex min-h-full w-full max-w-260 flex-col gap-8 py-10">
      <ProfileSummary
        name={data.name}
        email={data.email}
        profileImage={data.profile_image}
        projectCount={data.participating_project_count}
      />

      <div className="flex gap-6" role="tablist" aria-label="마이페이지 탭">
        {tabs.map((tab, index) => (
          <button
            key={tab.id}
            ref={(element) => {
              tabRefs.current[tab.id] = element;
            }}
            type="button"
            onClick={() => handleTabChange(tab.id)}
            onKeyDown={(event) => handleTabKeyDown(event, index)}
            role="tab"
            id={`mypage-${tab.id}-tab`}
            aria-selected={activeTab === tab.id}
            aria-controls={`mypage-${tab.id}-panel`}
            tabIndex={activeTab === tab.id ? 0 : -1}
            className={`typo-button-md cursor-pointer border-b-2 px-1 pb-0 transition-colors ${
              activeTab === tab.id
                ? "border-(--color-border-brand) text-(--color-text-brand)"
                : "border-transparent text-(--color-text-tertiary) hover:text-(--color-text-secondary)"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      <div
        id={`mypage-${activeTab}-panel`}
        role="tabpanel"
        aria-labelledby={`mypage-${activeTab}-tab`}
      >
        {activeTab === "activity" ? (
          <ActivityTab info={data} />
        ) : (
          <AccountTab info={data} />
        )}
      </div>
    </div>
  );
}

export default MyPage;
