import { isAxiosError } from "axios";
import { useLocation, useNavigate } from "react-router-dom";
import { logout } from "@/apis/auth";
import { ROUTES } from "@/constants/routes";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { tokenStore } from "@/utils/tokenStore";
import type { MenuItem } from "../types";

export const useSidebarNavigation = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { teamId } = useCurrentTeam();

  const handleMenuClick = async (item: MenuItem) => {
    if (item.id === "logout") {
      try {
        await logout();
        tokenStore.clearToken();
        navigate(ROUTES.LANDING);
      } catch (error) {
        console.error("로그아웃 요청이 실패해 로컬 세션만 정리했습니다.", {
          status: isAxiosError(error) ? error.response?.status : undefined,
        });
        tokenStore.clearToken();
        navigate(ROUTES.LANDING);
      }
      return;
    }

    if (item.path && teamId) {
      navigate(`/team/${teamId}${item.path}`);
    }
  };

  const isActive = (item: MenuItem) => {
    if (!item.path) return false;

    // 팀 경로에서 패턴 매칭
    if (item.path === "/") {
      return /^\/team\/\d+$/.test(location.pathname);
    }
    return location.pathname.includes(item.path);
  };

  return { handleMenuClick, isActive };
};
