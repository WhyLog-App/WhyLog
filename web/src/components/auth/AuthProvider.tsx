import { type ReactNode, useEffect, useState } from "react";
import { refreshAccessToken } from "@/apis/auth";
import { tokenStore } from "@/utils/tokenStore";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [isInitializing, setIsInitializing] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        // HttpOnly 쿠키의 refresh token으로 새 access token 발급
        const accessToken = await refreshAccessToken();
        if (!accessToken) {
          throw new Error("Invalid access token received");
        }
        tokenStore.setToken(accessToken);
      } catch (_error) {
        // Refresh 실패 시 토큰 정리
        tokenStore.clearToken();
        console.debug("세션 복구 실패, 로그인이 필요합니다");
      } finally {
        setIsInitializing(false);
      }
    };

    initializeAuth();
  }, []);

  if (isInitializing) {
    return (
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "100vh",
        }}
      >
        Loading...
      </div>
    );
  }

  return <>{children}</>;
}
