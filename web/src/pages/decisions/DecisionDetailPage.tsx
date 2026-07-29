import type { DecisionDetailViewModel } from "@/types/decision";
import ApplicationStatusCard from "./components/ApplicationStatusCard";
import CommitTableCard from "./components/CommitTableCard";
import ContextCard from "./components/ContextCard";
import DecisionHeader from "./components/DecisionHeader";
import ReasonsCard from "./components/ReasonsCard";
import TimelineCard from "./components/TimelineCard";

interface DecisionDetailPageProps {
  vm: DecisionDetailViewModel;
  decisionId: number;
}

const DecisionDetailPage = ({ vm, decisionId }: DecisionDetailPageProps) => {
  return (
    <div className="-mx-4 flex h-full flex-col gap-7 overflow-y-auto p-6 lg:-mx-20 lg:p-10 2xl:p-15 3xl:-mx-50">
      <DecisionHeader
        name={vm.detail.name}
        confidence={vm.confidence}
        meta={vm.meta}
      />

      <div className="flex min-h-0 flex-1 flex-col gap-5">
        <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
          <TimelineCard items={vm.detail.decision_timelines} />
          <ReasonsCard
            reasons={vm.detail.decision_reasons}
            count={vm.detail.decision_reason_count}
          />
          <ApplicationStatusCard commits={vm.linked_commits} />
        </div>

        <div className="flex min-h-0 flex-1 flex-col items-stretch gap-5 lg:flex-row">
          <ContextCard
            messages={vm.detail.decision_contexts}
            className="w-full lg:w-2/5 lg:min-w-90 lg:max-w-120"
          />
          <CommitTableCard
            decisionId={decisionId}
            applicationId={vm.detail.application_id}
            recommendedCommits={vm.recommended_commits}
            linkedCommits={vm.linked_commits}
            className="w-full min-w-0 flex-1"
          />
        </div>
      </div>
    </div>
  );
};

export default DecisionDetailPage;
