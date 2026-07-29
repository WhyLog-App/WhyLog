import ENDPOINT from "@/constants/endpoint";
import type { ApiResponse } from "@/types/auth";
import type { DecisionConfidence } from "@/types/decision";
import { http } from "@/utils/http";

export const getDecisionReliability = async (
  decisionId: number,
): Promise<DecisionConfidence> => {
  const { data } = await http.get<
    unknown,
    { data: ApiResponse<DecisionConfidence> }
  >(ENDPOINT.DECISIONS.RELIABILITY(decisionId));
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
  return data.result;
};

// POST /api/decisions/{decisionId}/commit/match
export const matchDecisionCommit = async (
  decisionId: number,
): Promise<void> => {
  const { data } = await http.post<unknown, { data: ApiResponse<unknown> }>(
    ENDPOINT.DECISIONS.MATCH_COMMIT(decisionId),
  );
  if (!data.isSuccess) {
    throw new Error(data.message);
  }
};
