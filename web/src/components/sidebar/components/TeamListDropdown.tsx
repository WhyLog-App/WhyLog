import IconAddPlus from "@/assets/icons/edit/ic_add_plus.svg?react";
import { Icon } from "@/components/common/Icon";
import { TeamImage } from "@/components/common/TeamImage";
import type { Team } from "@/types/team";

interface TeamListDropdownProps {
  teams: Team[];
  currentTeamId: number | null;
  onSelectTeam: (team: Team) => void;
  onCreateTeam: () => void;
  onInviteMember?: () => void;
}

export const TeamListDropdown = ({
  teams,
  currentTeamId,
  onSelectTeam,
  onCreateTeam,
  onInviteMember,
}: TeamListDropdownProps) => {
  return (
    <div className="absolute top-full left-0 z-50 mt-1 w-full rounded-lg border border-border-default bg-bg-surface py-1 shadow-lg">
      {teams.map((team) => (
        <button
          key={team.team_id}
          type="button"
          onClick={() => onSelectTeam(team)}
          className={`flex w-full cursor-pointer items-center gap-2 px-4 py-2.5 transition-colors hover:bg-action-hover
            ${currentTeamId === team.team_id ? "bg-action-active text-text-brand-darker" : "text-text-primary"}
          `}
        >
          <TeamImage
            src={team.team_image}
            alt={`${team.name} 이미지`}
            size={20}
          />
          <span className="typo-body5 truncate">{team.name}</span>
        </button>
      ))}

      {teams.length > 0 && <div className="mx-3 my-1 h-px bg-border-divider" />}

      {onInviteMember && currentTeamId !== null && (
        <button
          type="button"
          onClick={onInviteMember}
          className="flex w-full cursor-pointer items-center gap-2 px-4 py-2.5 text-text-secondary transition-colors hover:bg-action-hover"
        >
          <Icon icon={IconAddPlus} size={20} className="shrink-0" />
          <span className="typo-body5">팀원 초대</span>
        </button>
      )}

      <button
        type="button"
        onClick={onCreateTeam}
        className="flex w-full cursor-pointer items-center gap-2 px-4 py-2.5 text-text-secondary transition-colors hover:bg-action-hover"
      >
        <Icon icon={IconAddPlus} size={20} className="shrink-0" />
        <span className="typo-body5">새 팀 만들기</span>
      </button>
    </div>
  );
};
