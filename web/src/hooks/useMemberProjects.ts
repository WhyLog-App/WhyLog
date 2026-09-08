import { useInfiniteQuery } from "@tanstack/react-query";
import { useMemo } from "react";
import { getMemberProjects } from "@/apis/members";
import { memberProjectsQueryKey } from "@/hooks/memberQueryKeys";

export const useMemberProjects = (
  memberId: number | null,
  options?: { enabled?: boolean },
) => {
  const query = useInfiniteQuery({
    queryKey: memberProjectsQueryKey(memberId),
    queryFn: ({ pageParam }) => {
      if (memberId == null) {
        throw new Error("멤버 ID 없이 참여 프로젝트를 조회할 수 없습니다.");
      }
      return getMemberProjects(memberId, pageParam);
    },
    initialPageParam: undefined as number | undefined,
    getNextPageParam: (lastPage) =>
      lastPage.has_next && lastPage.next_cursor_id != null
        ? lastPage.next_cursor_id
        : undefined,
    enabled: memberId != null && (options?.enabled ?? true),
  });

  const projects = useMemo(
    () =>
      query.data?.pages.flatMap((page) => page.participating_projects) ?? [],
    [query.data],
  );

  return {
    ...query,
    projects,
  };
};
