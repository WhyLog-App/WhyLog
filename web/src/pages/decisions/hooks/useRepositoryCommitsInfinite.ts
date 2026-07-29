import { useInfiniteQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { getRepositoryCommits } from "@/apis/git";
import type {
  RepositoryCommitItem,
  RepositoryCommitListResult,
} from "@/types/git";

export const REPOSITORY_COMMITS_QUERY_KEY = [
  "repositories",
  "commits",
] as const;

export const useRepositoryCommitsInfinite = (
  repositoryId: number | null,
  options?: { enabled?: boolean },
) => {
  const query = useInfiniteQuery<
    RepositoryCommitListResult,
    Error,
    { pages: RepositoryCommitListResult[]; pageParams: (number | undefined)[] },
    readonly (string | number | null)[],
    number | undefined
  >({
    queryKey: [...REPOSITORY_COMMITS_QUERY_KEY, repositoryId],
    queryFn: ({ pageParam }) =>
      getRepositoryCommits(repositoryId as number, pageParam),
    initialPageParam: undefined,
    getNextPageParam: (lastPage) =>
      lastPage.has_next && lastPage.next_cursor_id != null
        ? lastPage.next_cursor_id
        : undefined,
    enabled: repositoryId != null && (options?.enabled ?? true),
  });

  const commits: RepositoryCommitItem[] = useMemo(
    () => query.data?.pages.flatMap((p) => p.commit_dtolist) ?? [],
    [query.data],
  );

  const totalCount = query.data?.pages[0]?.total_commit_count ?? 0;

  return {
    ...query,
    commits,
    totalCount,
  };
};
