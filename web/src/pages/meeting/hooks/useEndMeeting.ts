import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { endMeeting } from "@/apis/meetings";
import type { ApiResponse } from "@/types/auth";
import { MEETING_LIST_QUERY_KEY } from "./useMeetingList";

/**
 * 회의 종료 처리.
 * - 성공: ONGOING/COMPLETED 캐시 무효화 후 홈("/")으로 이동
 * - 실패: 에러 메시지를 hook에서 노출(silent fail 방지)
 */
export const useEndMeeting = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (id: number) => endMeeting(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: MEETING_LIST_QUERY_KEY });
      navigate("/");
    },
    onError: (err: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(err)) {
        setErrorMessage(
          err.response?.data?.message ?? "회의 종료에 실패했습니다.",
        );
        return;
      }
      setErrorMessage("회의 종료에 실패했습니다.");
    },
  });

  return {
    endMeeting: (id: number) => {
      setErrorMessage(null);
      mutation.mutate(id);
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
