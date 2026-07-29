import { useState } from "react";
import { useNavigate } from "react-router-dom";
import EmptyStateCard from "@/components/common/EmptyStateCard";
import CreateTeamModal from "@/components/panel/meeting/CreateTeamModal";
import { createTeamRoute } from "@/constants/routes";
import AppLayout from "@/layout/AppLayout";
import { useCreateTeam } from "@/pages/home/hooks/useCreateTeam";

function CreateTeamPage() {
  const navigate = useNavigate();
  const [isCreateTeamModalOpen, setIsCreateTeamModalOpen] = useState(false);

  const { createTeam, isPending } = useCreateTeam({
    onSuccess: (result) => {
      setIsCreateTeamModalOpen(false);
      navigate(createTeamRoute(result.team_id));
    },
  });

  const handleCreate = (teamName: string, photo: File | null) => {
    createTeam(teamName, photo ?? undefined);
  };

  return (
    <AppLayout>
      <div className="flex min-h-full w-full items-center justify-center p-8">
        <EmptyStateCard
          page="Home"
          onAction={() => setIsCreateTeamModalOpen(true)}
        />
        {isCreateTeamModalOpen && (
          <CreateTeamModal
            onClose={() => setIsCreateTeamModalOpen(false)}
            onCreate={handleCreate}
            isPending={isPending}
          />
        )}
      </div>
    </AppLayout>
  );
}

export default CreateTeamPage;
