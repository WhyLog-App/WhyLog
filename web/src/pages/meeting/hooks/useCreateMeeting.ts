import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createMeeting } from "@/apis/meetings";
import type { ApiResponse } from "@/types/auth";
import type { CreateMeetingResult } from "@/types/meeting";
import { MEETING_LIST_QUERY_KEY } from "./useMeetingList";

export const useCreateMeeting = (
  teamId: number | null,
  onSuccess?: () => void,
) => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: ({ name }: { name: string }) => {
      if (teamId == null) throw new Error("Team ID is required");
      return createMeeting(teamId, {
        name,
        start_date_time: new Date().toISOString(),
      });
    },
    onSuccess: (result: CreateMeetingResult) => {
      queryClient.invalidateQueries({ queryKey: MEETING_LIST_QUERY_KEY });
      onSuccess?.();
      navigate(`/team/${teamId}/meeting/${result.meeting_id}`, {
        state: { name: result.name },
      });
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "회의 생성에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("회의 생성에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    createMeeting: (name: string) => {
      setErrorMessage(null);
      mutation.mutate({ name });
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
