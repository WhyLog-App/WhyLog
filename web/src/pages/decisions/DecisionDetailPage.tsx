import { useState } from "react";
import type { DecisionDetailViewModel } from "@/types/decision";
import ApplicationDetailTabs, {
  type ApplicationDetailTab,
} from "./components/ApplicationDetailTabs";
import CodeApplicationTab from "./components/CodeApplicationTab";
import DecisionHeader from "./components/DecisionHeader";
import OriginalContextTab from "./components/OriginalContextTab";
import RelationshipMap from "./components/RelationshipMap";

interface DecisionDetailPageProps {
  vm: DecisionDetailViewModel;
}

const DecisionDetailPage = ({ vm }: DecisionDetailPageProps) => {
  const [activeTab, setActiveTab] = useState<ApplicationDetailTab>("code");

  return (
    <div className="-mx-4 flex h-full flex-col gap-4 overflow-y-auto p-6 lg:-mx-20 lg:p-10 2xl:p-15 3xl:-mx-50">
      <DecisionHeader meta={vm.meta} />
      <ApplicationDetailTabs activeTab={activeTab} onTabChange={setActiveTab} />
      {activeTab === "context" ? (
        <OriginalContextTab
          messages={vm.detail.decision_contexts}
          reasons={vm.detail.decision_reasons}
        />
      ) : activeTab === "relation" ? (
        <RelationshipMap />
      ) : (
        <CodeApplicationTab
          applicationId={vm.detail.application_id}
          applicationName={vm.detail.name}
          keywords={vm.detail.decision_reasons.map((reason) => reason.title)}
          recommendedCommits={vm.recommended_commits}
          linkedCommits={vm.linked_commits}
        />
      )}
    </div>
  );
};

export default DecisionDetailPage;
