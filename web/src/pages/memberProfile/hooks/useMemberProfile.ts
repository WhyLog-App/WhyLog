import { useQuery } from "@tanstack/react-query";
import { getMemberProfile } from "@/apis/members";
import {
  MEMBER_PROFILE_QUERY_KEY,
  memberProfileQueryKey,
} from "@/hooks/memberQueryKeys";
import type { MemberProfile } from "@/types/member";

export const useMemberProfile = (memberId: number | null) =>
  useQuery<MemberProfile>({
    queryKey:
      memberId == null
        ? [...MEMBER_PROFILE_QUERY_KEY, null]
        : memberProfileQueryKey(memberId),
    queryFn: () => {
      if (memberId == null) {
        throw new Error("멤버 ID 없이 프로필을 조회할 수 없습니다.");
      }
      return getMemberProfile(memberId);
    },
    enabled: memberId != null,
  });
