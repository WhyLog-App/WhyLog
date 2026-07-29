import { useQuery } from "@tanstack/react-query";
import { checkGitHubTokenStatus } from "@/apis/git";

export const useCheckGitHubToken = () => {
  return useQuery({
    queryKey: ["github-token-status"],
    queryFn: checkGitHubTokenStatus,
    staleTime: 0,
    gcTime: 0,
  });
};
