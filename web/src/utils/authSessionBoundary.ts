import { appQueryClient } from "@/utils/queryClient";
import { tokenStore } from "@/utils/tokenStore";

export const establishAuthenticatedSession = (accessToken: string) => {
  appQueryClient.clear();
  tokenStore.setToken(accessToken);
};

export const clearAuthenticatedSession = () => {
  appQueryClient.clear();
  tokenStore.clearToken();
};
