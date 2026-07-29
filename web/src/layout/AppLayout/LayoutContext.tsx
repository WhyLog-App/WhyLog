import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useLocation } from "react-router-dom";

interface LayoutContextValue {
  isSidebarOpen: boolean;
  isPanelOpen: boolean;
  openSidebar: () => void;
  closeSidebar: () => void;
  openPanel: () => void;
  closePanel: () => void;
  closeAll: () => void;
}

const LayoutContext = createContext<LayoutContextValue | null>(null);

export const LayoutProvider = ({ children }: { children: ReactNode }) => {
  const { pathname } = useLocation();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isPanelOpen, setIsPanelOpen] = useState(false);

  const openSidebar = useCallback(() => setIsSidebarOpen(true), []);
  const closeSidebar = useCallback(() => setIsSidebarOpen(false), []);
  const openPanel = useCallback(() => setIsPanelOpen(true), []);
  const closePanel = useCallback(() => setIsPanelOpen(false), []);
  const closeAll = useCallback(() => {
    setIsSidebarOpen(false);
    setIsPanelOpen(false);
  }, []);

  // 라우트 변경 시 드로어 자동 닫힘 (메뉴 클릭/팀 선택/리다이렉트 모두 커버)
  // biome-ignore lint/correctness/useExhaustiveDependencies: pathname 변경을 트리거로 사용
  useEffect(() => {
    setIsSidebarOpen(false);
    setIsPanelOpen(false);
  }, [pathname]);

  const isAnyOpen = isSidebarOpen || isPanelOpen;

  // 데스크톱 폭으로 전환되면 모바일 드로어 상태 정리
  useEffect(() => {
    const mql = window.matchMedia("(min-width: 1024px)");
    const handleChange = () => {
      if (mql.matches) closeAll();
    };
    handleChange();
    mql.addEventListener("change", handleChange);
    return () => mql.removeEventListener("change", handleChange);
  }, [closeAll]);

  // 드로어 열림 시 body 스크롤 락
  useEffect(() => {
    if (!isAnyOpen) return;

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, [isAnyOpen]);

  // Escape 키로 전체 닫기
  useEffect(() => {
    if (!isAnyOpen) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") closeAll();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [isAnyOpen, closeAll]);

  const value = useMemo<LayoutContextValue>(
    () => ({
      isSidebarOpen,
      isPanelOpen,
      openSidebar,
      closeSidebar,
      openPanel,
      closePanel,
      closeAll,
    }),
    [
      isSidebarOpen,
      isPanelOpen,
      openSidebar,
      closeSidebar,
      openPanel,
      closePanel,
      closeAll,
    ],
  );

  return (
    <LayoutContext.Provider value={value}>{children}</LayoutContext.Provider>
  );
};

export const useLayout = (): LayoutContextValue => {
  const context = useContext(LayoutContext);
  if (!context) {
    throw new Error("useLayout must be used within a LayoutProvider");
  }
  return context;
};
