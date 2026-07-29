import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { deleteTeam } from "@/apis/teams";
import { TEAMS_QUERY_KEY } from "@/components/sidebar/hooks/useTeams";
import type { ApiResponse } from "@/types/auth";

/**
 * 팀 삭제 처리.
 * - 성공: 팀 목록 캐시 무효화 후 홈("/")으로 이동 (RootRedirect가 다른 팀/온보딩으로 분기)
 * - 실패: 에러 메시지를 hook에서 노출
 */
export const useDeleteTeam = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (id: number) => deleteTeam(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: TEAMS_QUERY_KEY });
      navigate("/");
    },
    onError: (err: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(err)) {
        setErrorMessage(
          err.response?.data?.message ?? "팀 삭제에 실패했습니다.",
        );
        return;
      }
      setErrorMessage("팀 삭제에 실패했습니다.");
    },
  });

  return {
    deleteTeam: (id: number) => {
      setErrorMessage(null);
      mutation.mutate(id);
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
