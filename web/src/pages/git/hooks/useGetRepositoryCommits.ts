import { useInfiniteQuery } from "@tanstack/react-query";
import { getRepositoryCommits } from "@/apis/git";

export const useGetRepositoryCommits = (repositoryId: number | null) => {
  return useInfiniteQuery({
    queryKey: ["repository-commits", repositoryId],
    queryFn: ({ pageParam }) =>
      getRepositoryCommits(repositoryId as number, pageParam),
    enabled: repositoryId !== null,
    initialPageParam: undefined as number | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.has_next ? (lastPage.next_cursor_id ?? undefined) : undefined,
  });
};
