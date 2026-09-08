import { useState } from "react";
import GlassCard from "@/components/common/GlassCard";
import Modal from "@/components/common/Modal";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { useDeleteTeam } from "./hooks/useDeleteTeam";

const SettingsPage = () => {
  const { teamId, currentTeam, isLoading } = useCurrentTeam();
  const {
    deleteTeam,
    isPending: isDeleting,
    errorMessage: deleteError,
  } = useDeleteTeam();
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  return (
    <div className="flex h-full flex-col gap-6 py-10">
      <header className="flex flex-col gap-1">
        <h1 className="typo-h4 text-(--color-text-primary)">설정</h1>
        <p className="typo-body6 text-(--color-text-tertiary)">
          팀 정보를 관리합니다.
        </p>
      </header>

      <GlassCard className="gap-3 px-6 py-5">
        <h2 className="typo-subtitle3 text-(--color-text-primary)">팀 정보</h2>
        <dl className="flex flex-col gap-2">
          <div className="flex items-center gap-3">
            <dt className="typo-body6 text-(--color-text-tertiary) w-20">
              팀 이름
            </dt>
            <dd className="typo-body4 text-(--color-text-primary)">
              {isLoading ? "불러오는 중..." : (currentTeam?.name ?? "-")}
            </dd>
          </div>
        </dl>
      </GlassCard>

      <div>
        <button
          type="button"
          className="typo-button-md cursor-pointer rounded-xl border border-red-300 bg-white px-5 py-2.5 text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
          disabled={teamId == null || isDeleting}
          onClick={() => setIsDeleteModalOpen(true)}
        >
          팀 삭제
        </button>
      </div>

      {isDeleteModalOpen && teamId != null && (
        <Modal
          title="팀을 삭제하시겠습니까?"
          onClose={() => !isDeleting && setIsDeleteModalOpen(false)}
          primaryLabel={isDeleting ? "삭제 중..." : "삭제"}
          onPrimaryClick={() => deleteTeam(teamId)}
          isPrimaryDisabled={isDeleting}
        >
          <div className="flex flex-col gap-2">
            <p className="typo-body4 text-(--color-text-secondary)">
              {currentTeam?.name
                ? `"${currentTeam.name}" 팀의 팀원, 회의, 저장소 정보가 모두 삭제되며 복구할 수 없습니다.`
                : "팀의 팀원, 회의, 저장소 정보가 모두 삭제되며 복구할 수 없습니다."}
            </p>
            {deleteError && (
              <p className="typo-caption text-red-500">{deleteError}</p>
            )}
          </div>
        </Modal>
      )}
    </div>
  );
};

export default SettingsPage;
