import { useQuery } from "@tanstack/react-query";
import { getRepositories } from "@/apis/git";

export const useGetRepositories = (teamId: number | null) => {
  return useQuery({
    queryKey: ["repositories", teamId],
    queryFn: () => getRepositories(teamId as number),
    enabled: teamId !== null,
  });
};
