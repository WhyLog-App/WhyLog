import { useState } from "react";
import { useMediaQuery } from "@/hooks/useMediaQuery";
import { useLayout } from "@/layout/AppLayout/LayoutContext";
import { SidebarFooter } from "./components/SidebarFooter";
import { SidebarHeader } from "./components/SidebarHeader";
import { SidebarNav } from "./components/SidebarNav";

const Sidebar = () => {
  const [hoverOpen, setHoverOpen] = useState(false);
  const isDesktop = useMediaQuery("(min-width: 1024px)");
  const { isSidebarOpen } = useLayout();

  // 모바일 드로어는 항상 확장 상태로 텍스트를 노출하고, 데스크톱은 hover로 토글
  const isExpanded = isDesktop ? hoverOpen : true;

  return (
    <aside
      id="app-sidebar"
      onMouseEnter={isDesktop ? () => setHoverOpen(true) : undefined}
      onMouseLeave={isDesktop ? () => setHoverOpen(false) : undefined}
      className={`fixed inset-y-0 left-0 z-40 flex h-screen w-58 shrink-0 flex-col gap-6 overflow-hidden border-r border-solid border-border-divider bg-white px-4 pb-8 pt-6 transition-all duration-300 lg:static lg:z-auto lg:translate-x-0
        ${isSidebarOpen ? "translate-x-0" : "-translate-x-full"}
        ${hoverOpen ? "lg:w-58" : "lg:w-20"}
      `}
    >
      <SidebarHeader isOpen={isExpanded} />
      <SidebarNav isOpen={isExpanded} />
      <SidebarFooter isOpen={isExpanded} />
    </aside>
  );
};

export default Sidebar;
