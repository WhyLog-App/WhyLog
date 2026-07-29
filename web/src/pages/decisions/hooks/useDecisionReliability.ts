import { useQuery } from "@tanstack/react-query";
import { getDecisionReliability } from "@/apis/decisions";
import type { DecisionConfidence } from "@/types/decision";

export const DECISION_RELIABILITY_QUERY_KEY = [
  "decisions",
  "reliability",
] as const;

export const useDecisionReliability = (
  decisionId: number | null,
  options?: { enabled?: boolean },
) =>
  useQuery<DecisionConfidence>({
    queryKey: [...DECISION_RELIABILITY_QUERY_KEY, decisionId],
    queryFn: () => getDecisionReliability(decisionId as number),
    enabled: decisionId != null && (options?.enabled ?? true),
  });
