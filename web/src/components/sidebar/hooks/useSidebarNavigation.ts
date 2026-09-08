import { isAxiosError } from "axios";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { logout } from "@/apis/auth";
import { ROUTES } from "@/constants/routes";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import { clearAuthenticatedSession } from "@/utils/authSessionBoundary";
import type { MenuItem } from "../types";

export const useSidebarNavigation = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { teamId } = useCurrentTeam();
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [logoutWarningMessage, setLogoutWarningMessage] = useState<
    string | null
  >(null);

  const handleMenuClick = (item: MenuItem) => {
    if (item.id === "logout") {
      setLogoutWarningMessage(null);
      setIsLogoutModalOpen(true);
      return;
    }

    if (!item.path) return;

    if (item.isGlobal) {
      navigate(item.path);
      return;
    }

    if (teamId) {
      navigate(`/team/${teamId}${item.path}`);
    }
  };

  const closeLogoutModal = () => {
    if (isLoggingOut) return;
    setIsLogoutModalOpen(false);
    setLogoutWarningMessage(null);
  };

  const confirmLogout = async () => {
    if (isLoggingOut) return;
    setLogoutWarningMessage(null);
    setIsLoggingOut(true);

    try {
      await logout();
      clearAuthenticatedSession();
      void clearAuthFlowState();
      setIsLogoutModalOpen(false);
      navigate(ROUTES.LANDING);
    } catch (error) {
      console.error("로그아웃 요청에 실패했습니다.", {
        status: isAxiosError(error) ? error.response?.status : undefined,
      });
      setLogoutWarningMessage(
        "로그아웃에 실패했습니다. 네트워크 연결을 확인한 뒤 다시 시도해 주세요.",
      );
    } finally {
      setIsLoggingOut(false);
    }
  };

  const isActive = (item: MenuItem) => {
    if (!item.path) return false;

    if (item.isGlobal) {
      return location.pathname === item.path;
    }

    // 팀 경로에서 패턴 매칭
    if (item.path === "/") {
      return /^\/team\/\d+$/.test(location.pathname);
    }
    return location.pathname.includes(item.path);
  };

  return {
    handleMenuClick,
    isActive,
    isLogoutModalOpen,
    isLoggingOut,
    closeLogoutModal,
    confirmLogout,
    logoutWarningMessage,
  };
};
