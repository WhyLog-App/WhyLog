import { isAxiosError } from "axios";
import { type ReactNode, useEffect, useState } from "react";
import { refreshAccessToken } from "@/apis/auth";
import { clearAuthFlowState } from "@/utils/authFlowStorage";
import {
  clearAuthenticatedSession,
  establishAuthenticatedSession,
} from "@/utils/authSessionBoundary";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    let isActive = true;

    const initializeAuth = async () => {
      try {
        // HttpOnly 쿠키의 refresh token으로 새 access token 발급
        const accessToken = await refreshAccessToken();
        if (!accessToken) {
          throw new Error("Invalid access token received");
        }
        if (!isActive) return;
        establishAuthenticatedSession(accessToken);
        void clearAuthFlowState();
      } catch (error) {
        if (!isActive) return;
        // Refresh 실패 시 토큰 정리
        clearAuthenticatedSession();
        console.debug("세션 복구 실패, 로그인이 필요합니다", {
          status: isAxiosError(error) ? error.response?.status : undefined,
        });
      } finally {
        if (isActive) setIsInitializing(false);
      }
    };

    void initializeAuth();
    return () => {
      isActive = false;
    };
  }, []);

  if (isInitializing) {
    return (
      <div className="flex h-dvh items-center justify-center">Loading...</div>
    );
  }

  return <>{children}</>;
}
