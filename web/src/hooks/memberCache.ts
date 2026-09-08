import type { QueryClient } from "@tanstack/react-query";
import {
  MY_INFO_QUERY_KEY,
  memberProfileQueryKey,
  memberProjectsQueryKey,
} from "@/hooks/memberQueryKeys";
import { APPLICATION_DETAIL_QUERY_KEY } from "@/pages/decisions/hooks/useApplicationDetail";
import { MEETING_DETAIL_QUERY_KEY } from "@/pages/meeting/hooks/useMeetingDetail";
import { MEETING_HISTORY_QUERY_KEY } from "@/pages/meeting/hooks/useMeetingHistory";
import type { MemberProfile, MyInfo, ProfileVisibility } from "@/types/member";

type MemberIdentityPatch = Partial<
  Pick<MyInfo, "name" | "email" | "profile_image">
>;

const markMemberIdentityStale = (
  queryClient: QueryClient,
  memberId: number,
) => {
  void queryClient.invalidateQueries({
    queryKey: MY_INFO_QUERY_KEY,
    refetchType: "none",
  });
  void queryClient.invalidateQueries({
    queryKey: memberProfileQueryKey(memberId),
    refetchType: "none",
  });
  for (const queryKey of [
    MEETING_DETAIL_QUERY_KEY,
    MEETING_HISTORY_QUERY_KEY,
    APPLICATION_DETAIL_QUERY_KEY,
  ]) {
    void queryClient.invalidateQueries({ queryKey });
  }
};

export const patchMemberIdentityCache = (
  queryClient: QueryClient,
  memberId: number,
  patch: MemberIdentityPatch,
) => {
  queryClient.setQueryData<MyInfo>(MY_INFO_QUERY_KEY, (current) =>
    current ? { ...current, ...patch } : current,
  );
  queryClient.setQueryData<MemberProfile>(
    memberProfileQueryKey(memberId),
    (current) => {
      if (!current || current.profile_view === "WITHDRAWN") {
        return current;
      }

      return { ...current, ...patch };
    },
  );
  markMemberIdentityStale(queryClient, memberId);
};

export const patchOwnProfileVisibilityCache = (
  queryClient: QueryClient,
  memberId: number,
  profileVisibility: ProfileVisibility,
) => {
  queryClient.setQueryData<MyInfo>(MY_INFO_QUERY_KEY, (current) =>
    current ? { ...current, profile_visibility: profileVisibility } : current,
  );
  void queryClient.invalidateQueries({
    queryKey: MY_INFO_QUERY_KEY,
    refetchType: "none",
  });
  queryClient.removeQueries({
    queryKey: memberProfileQueryKey(memberId),
    exact: true,
  });
  void queryClient.invalidateQueries({
    queryKey: memberProjectsQueryKey(memberId),
    refetchType: "none",
  });
};
