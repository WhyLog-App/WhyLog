import type React from "react";
import Panel from "@/components/panel";
import Sidebar from "@/components/sidebar";
import { LayoutProvider, useLayout } from "./LayoutContext";
import MobileTopBar from "./MobileTopBar";

const APP_BACKGROUND =
  "linear-gradient(-19.8deg, rgba(208,240,244,0.5) 10.78%, rgba(232,228,248,0.5) 42.16%, rgba(216,228,252,0.5) 89.22%), linear-gradient(#ffffff,#ffffff)";

const AppLayoutInner = (props: { children?: React.ReactNode }) => {
  const { isSidebarOpen, isPanelOpen, closeAll } = useLayout();
  const isAnyDrawerOpen = isSidebarOpen || isPanelOpen;

  return (
    <div className="flex h-screen">
      <MobileTopBar />
      <Sidebar />
      <Panel />
      {isAnyDrawerOpen && (
        <button
          type="button"
          aria-label="메뉴 닫기"
          onClick={closeAll}
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
        />
      )}
      <main
        className="w-full flex-1 overflow-auto px-4 pt-14 lg:w-auto lg:px-20 lg:pt-0 3xl:px-50"
        style={{ backgroundImage: APP_BACKGROUND }}
      >
        {props.children}
      </main>
    </div>
  );
};

const AppLayout = (props: { children?: React.ReactNode }) => {
  return (
    <LayoutProvider>
      <AppLayoutInner>{props.children}</AppLayoutInner>
    </LayoutProvider>
  );
};

export default AppLayout;
