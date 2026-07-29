import { useLocation } from "react-router-dom";
import IconFilter from "@/assets/icons/menu/ic_filter.svg?react";
import IconMenuBurger from "@/assets/icons/menu/ic_menu_burger.svg?react";
import { Icon } from "@/components/common/Icon";
import LogoSymbol from "@/components/logo/LogoSymbol";
import { hasPanelForPath } from "@/components/panel/hasPanelForPath";
import { useLayout } from "./LayoutContext";

const MobileTopBar = () => {
  const { pathname } = useLocation();
  const { isSidebarOpen, isPanelOpen, openSidebar, openPanel } = useLayout();
  const showPanelButton = hasPanelForPath(pathname);

  return (
    <header className="fixed inset-x-0 top-0 z-20 flex h-14 items-center gap-2 border-b border-solid border-border-divider bg-white px-4 lg:hidden">
      <button
        type="button"
        onClick={openSidebar}
        aria-label="메뉴 열기"
        aria-expanded={isSidebarOpen}
        aria-controls="app-sidebar"
        className="flex size-10 cursor-pointer items-center justify-center rounded-lg text-text-secondary transition-colors hover:bg-action-hover"
      >
        <Icon icon={IconMenuBurger} size={22} />
      </button>

      <div className="flex flex-1 items-center justify-center">
        <LogoSymbol className="h-7 w-8" aria-hidden="true" />
      </div>

      {showPanelButton ? (
        <button
          type="button"
          onClick={openPanel}
          aria-label="패널 열기"
          aria-expanded={isPanelOpen}
          aria-controls="app-panel"
          className="flex size-10 cursor-pointer items-center justify-center rounded-lg text-text-secondary transition-colors hover:bg-action-hover"
        >
          <Icon icon={IconFilter} size={22} />
        </button>
      ) : (
        <span className="size-10" aria-hidden="true" />
      )}
    </header>
  );
};

export default MobileTopBar;
