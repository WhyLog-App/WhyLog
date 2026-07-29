import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import IconChevronDown from "@/assets/icons/arrow/ic_chevron_down.svg?react";
import IconSearch from "@/assets/icons/interface/ic_search.svg?react";
import { Icon } from "@/components/common/Icon";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { parseRouteId } from "@/utils/parseRouteId";
import { useDecisions } from "./hooks/useDecisions";

const DecisionPanel = () => {
  const navigate = useNavigate();
  const { teamId } = useCurrentTeam();
  const {
    decisionId: activeDecisionIdParam,
    applicationId: activeApplicationIdParam,
  } = useParams<{
    decisionId?: string;
    applicationId?: string;
  }>();
  const activeDecisionId = parseRouteId(activeDecisionIdParam);
  const activeApplicationId = parseRouteId(activeApplicationIdParam);
  const { data, isLoading, isError } = useDecisions(teamId);

  const [keyword, setKeyword] = useState("");
  const normalizedKeyword = keyword.trim();

  const [expandedIds, setExpandedIds] = useState<Set<number>>(new Set());

  useEffect(() => {
    if (activeDecisionId == null) return;
    setExpandedIds((prev) => {
      if (prev.has(activeDecisionId)) return prev;
      const next = new Set(prev);
      next.add(activeDecisionId);
      return next;
    });
  }, [activeDecisionId]);

  const decisions = useMemo(() => {
    const list = data ?? [];
    if (!normalizedKeyword) return list;
    const lower = normalizedKeyword.toLowerCase();
    return list.filter((d) => d.name.toLowerCase().includes(lower));
  }, [data, normalizedKeyword]);

  const hasDecisions = decisions.length > 0;

  const toggleExpanded = (decisionId: number) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(decisionId)) {
        next.delete(decisionId);
      } else {
        next.add(decisionId);
      }
      return next;
    });
  };

  return (
    <>
      <div className="flex w-full items-center justify-between px-5">
        <h2 className="typo-h6 text-(--color-text-primary)">결정사항 목록</h2>
        <span className="typo-subtitle5 text-(--color-text-tertiary)">
          매칭
        </span>
      </div>

      <div className="h-px w-full bg-(--color-border-divider)" />

      {isLoading ? (
        <div className="flex w-full flex-1 items-center justify-center px-4 typo-subtitle5 text-(--color-text-tertiary)">
          불러오는 중...
        </div>
      ) : isError ? (
        <div className="flex w-full flex-1 items-center justify-center px-4 typo-subtitle5 text-(--color-text-tertiary)">
          결정사항을 불러오지 못했습니다
        </div>
      ) : !hasDecisions && !normalizedKeyword ? (
        <div className="flex w-full flex-1 flex-col items-center justify-center px-4 text-center">
          <div className="typo-subtitle4 text-(--color-text-secondary)">
            아직 분석된 회의가
            <br />
            없습니다
          </div>
          <div className="mt-2 typo-body6 text-(--color-text-tertiary)">
            음성 회의를 진행하거나 녹음 파일을 업로드하면
            <br />
            결정사항이 자동으로 추출됩니다
          </div>
        </div>
      ) : (
        <div className="flex w-full flex-1 flex-col gap-2 overflow-y-auto px-4">
          <div className="flex w-full items-center gap-2 rounded-lg bg-(--color-bg-subtle) p-3">
            <Icon
              icon={IconSearch}
              size={24}
              className="shrink-0 text-(--color-text-brand)"
            />
            <input
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              aria-label="회의 이름 검색"
              placeholder="회의 이름으로 검색하세요..."
              className="w-full bg-transparent typo-subtitle5 text-(--color-text-primary) placeholder:text-(--color-text-disabled) focus:outline-none"
            />
          </div>

          <ul className="flex w-full flex-col gap-2">
            {decisions.map((decision) => {
              const isActive = activeDecisionId === decision.decision_id;
              const isExpanded = expandedIds.has(decision.decision_id);
              return (
                <li
                  key={decision.decision_id}
                  className="flex w-full flex-col rounded-lg bg-(--color-bg-surface)"
                >
                  <button
                    type="button"
                    onClick={() => toggleExpanded(decision.decision_id)}
                    aria-expanded={isExpanded}
                    className="flex w-full cursor-pointer items-center justify-between rounded-lg px-4 py-3 text-left hover:bg-(--color-action-hover)"
                  >
                    <span className="typo-subtitle5 text-(--color-text-primary)">
                      {decision.name}
                    </span>
                    <Icon
                      icon={IconChevronDown}
                      size={20}
                      className={`shrink-0 text-(--color-text-tertiary) transition-transform ${
                        isExpanded ? "rotate-180" : ""
                      }`}
                    />
                  </button>

                  {isExpanded &&
                    (decision.applications.length === 0 ? (
                      <div className="px-4 pb-3 typo-caption1 text-(--color-text-tertiary)">
                        연관된 적용 사항이 없습니다
                      </div>
                    ) : (
                      <ul className="flex w-full flex-col gap-1 px-2 pb-2">
                        {decision.applications.map((application) => (
                          <li key={application.application_id}>
                            <button
                              type="button"
                              onClick={() =>
                                teamId &&
                                navigate(
                                  `/team/${teamId}/decisions/${decision.decision_id}/${application.application_id}`,
                                )
                              }
                              aria-pressed={
                                isActive &&
                                activeApplicationId ===
                                  application.application_id
                              }
                              className={`flex w-full cursor-pointer items-center gap-2 rounded-md px-3 py-2 text-left ${
                                isActive &&
                                activeApplicationId ===
                                  application.application_id
                                  ? "bg-(--color-action-active)"
                                  : "hover:bg-(--color-action-hover)"
                              }`}
                            >
                              <span
                                aria-hidden="true"
                                className="inline-block h-2 w-2 shrink-0 rounded-full bg-(--color-text-brand)"
                              />
                              <span
                                className={`typo-subtitle5 ${
                                  isActive &&
                                  activeApplicationId ===
                                    application.application_id
                                    ? "text-(--color-text-brand)"
                                    : "text-(--color-text-primary)"
                                }`}
                              >
                                {application.name}
                              </span>
                            </button>
                          </li>
                        ))}
                      </ul>
                    ))}
                </li>
              );
            })}
          </ul>

          {!hasDecisions && normalizedKeyword && (
            <div className="flex w-full flex-1 items-center justify-center py-8 typo-subtitle5 text-(--color-text-tertiary)">
              검색 결과가 없습니다
            </div>
          )}
        </div>
      )}
    </>
  );
};

export default DecisionPanel;
