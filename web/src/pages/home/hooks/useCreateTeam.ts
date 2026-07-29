import { useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { useState } from "react";
import { createTeam } from "@/apis/teams";
import { TEAMS_QUERY_KEY } from "@/components/sidebar/hooks/useTeams";
import type { ApiResponse } from "@/types/auth";
import type { CreateTeamRequest, CreateTeamResult, Team } from "@/types/team";

interface UseCreateTeamOptions {
  onSuccess?: (result: CreateTeamResult) => void;
}

export const useCreateTeam = (options?: UseCreateTeamOptions) => {
  const queryClient = useQueryClient();
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (payload: CreateTeamRequest) => createTeam(payload),
    onSuccess: async (result: CreateTeamResult) => {
      const newTeam: Team = {
        team_id: result.team_id,
        name: result.name,
        team_image: result.team_image ?? result.image_url ?? null,
      };
      queryClient.setQueryData<Team[]>(TEAMS_QUERY_KEY, (prev) => {
        const list = prev ?? [];
        if (list.some((t) => t.team_id === newTeam.team_id)) return list;
        return [...list, newTeam];
      });

      options?.onSuccess?.(result);
      queryClient.invalidateQueries({ queryKey: TEAMS_QUERY_KEY });
    },
    onError: (error: unknown) => {
      if (isAxiosError<ApiResponse<unknown>>(error)) {
        setErrorMessage(
          error.response?.data?.message ??
            "팀 생성에 실패했습니다. 다시 시도해주세요.",
        );
        return;
      }
      setErrorMessage("팀 생성에 실패했습니다. 다시 시도해주세요.");
    },
  });

  return {
    createTeam: (name: string, image?: File) => {
      setErrorMessage(null);
      mutation.mutate({ name, image });
    },
    isPending: mutation.isPending,
    errorMessage,
  };
};
