import { useQuery } from "@tanstack/react-query";
import { getMyInfo } from "@/apis/members";
import { MY_INFO_QUERY_KEY } from "@/hooks/memberQueryKeys";
import type { MyInfo } from "@/types/member";

export const useMyInfo = () =>
  useQuery<MyInfo>({
    queryKey: MY_INFO_QUERY_KEY,
    queryFn: getMyInfo,
  });
