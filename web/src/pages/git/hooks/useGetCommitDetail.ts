import { useQuery } from "@tanstack/react-query";
import { getCommitDetail } from "@/apis/git";

export const useGetCommitDetail = (
  repositoryId: number | null,
  commitHash?: string,
) => {
  return useQuery({
    queryKey: ["commit-detail", repositoryId, commitHash],
    queryFn: () =>
      getCommitDetail(repositoryId as number, commitHash as string),
    enabled: repositoryId !== null && !!commitHash,
  });
};

export default useGetCommitDetail;
