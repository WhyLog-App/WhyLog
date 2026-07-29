import type { ReactNode } from "react";
import { useLocation } from "react-router-dom";
import { useLayout } from "@/layout/AppLayout/LayoutContext";
import DecisionPanel from "./decision/DecisionPanel";
import GitPanel from "./git/GitPanel";
import { hasPanelForPath } from "./hasPanelForPath";
import MeetingPanel from "./meeting/MeetingPanel";

const Panel = () => {
  const { pathname } = useLocation();
  const { isPanelOpen } = useLayout();

  if (!hasPanelForPath(pathname)) return null;

  let content: ReactNode = null;
  if (/\/decisions(\/|$)/.test(pathname)) {
    content = <DecisionPanel />;
  } else if (/\/meeting(\/|$)/.test(pathname)) {
    content = <MeetingPanel />;
  } else if (/\/git(\/|$)/.test(pathname)) {
    content = <GitPanel />;
  }

  return (
    <aside
      id="app-panel"
      className={`fixed inset-y-0 left-0 z-40 flex h-screen w-[85vw] max-w-70 shrink-0 flex-col gap-4 overflow-hidden border-r border-solid border-(--color-border-default) bg-(--color-bg-surface) pt-10 pb-8 shadow-[0px_0px_2px_0px_rgba(40,41,61,0.04),0px_4px_8px_0px_rgba(96,97,112,0.16)] transition-transform duration-300 lg:static lg:w-70 lg:max-w-none lg:translate-x-0
        ${isPanelOpen ? "translate-x-0" : "-translate-x-full"}
      `}
    >
      {content}
    </aside>
  );
};

export default Panel;
