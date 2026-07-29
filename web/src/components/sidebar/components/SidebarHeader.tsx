import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import IconChevronDown from "@/assets/icons/arrow/ic_chevron_down.svg?react";
import IconAddPlus from "@/assets/icons/edit/ic_add_plus.svg?react";
import IconMenuBurger from "@/assets/icons/menu/ic_menu_burger.svg?react";
import { Icon } from "@/components/common/Icon";
import { TeamImage } from "@/components/common/TeamImage";
import CreateTeamModal from "@/components/panel/meeting/CreateTeamModal";
import InviteTeamMemberModal from "@/components/panel/meeting/InviteTeamMemberModal";
import { createTeamRoute } from "@/constants/routes";
import { useCurrentTeam } from "@/hooks/useCurrentTeam";
import { useCreateTeam } from "@/pages/home/hooks/useCreateTeam";
import type { Team } from "@/types/team";
import { useInviteTeamMember } from "../hooks/useInviteTeamMember";
import { useTeams } from "../hooks/useTeams";
import { TeamListDropdown } from "./TeamListDropdown";

interface SidebarHeaderProps {
  isOpen: boolean;
}

export const SidebarHeader = ({ isOpen }: SidebarHeaderProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { currentTeam } = useCurrentTeam();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const { data: teams = [] } = useTeams();

  const { createTeam, isPending } = useCreateTeam({
    onSuccess: (result) => {
      setIsModalOpen(false);
      navigate(createTeamRoute(result.team_id));
    },
  });

  const {
    invite,
    isPending: isInvitePending,
    errorMessage: inviteErrorMessage,
    resetError: resetInviteError,
  } = useInviteTeamMember(currentTeam?.team_id ?? null, {
    onSuccess: () => {
      setIsInviteModalOpen(false);
    },
  });

  useEffect(() => {
    if (!isOpen) {
      setIsDropdownOpen(false);
    }
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target as Node)
      ) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSelectTeam = (team: Team) => {
    setIsDropdownOpen(false);

    // 현재 경로에서 팀 부분 제거
    const pathWithoutTeam = location.pathname.replace(/^\/team\/\d+/, "");

    // 새 팀으로 동일한 페이지 이동
    navigate(`/team/${team.team_id}${pathWithoutTeam}`);
  };

  const handleCreateTeam = () => {
    setIsDropdownOpen(false);
    setIsModalOpen(true);
  };

  const handleInviteMember = () => {
    setIsDropdownOpen(false);
    resetInviteError();
    setIsInviteModalOpen(true);
  };

  const handleModalCreate = (teamName: string, photo: File | null) => {
    createTeam(teamName, photo ?? undefined);
  };

  const hasTeams = teams.length > 0;

  return (
    <>
      <div className="relative flex items-center gap-1" ref={dropdownRef}>
        <div className="hidden size-12 shrink-0 items-center justify-center lg:flex">
          <Icon icon={IconMenuBurger} size={20} className="h-3.5" />
        </div>
        {isOpen && (
          <>
            {hasTeams ? (
              <button
                type="button"
                onClick={() => setIsDropdownOpen((prev) => !prev)}
                className={`group flex cursor-pointer items-center gap-1 rounded-lg px-2 py-1 transition-colors ${
                  isDropdownOpen ? "bg-action-active" : "hover:bg-action-hover"
                }`}
              >
                <TeamImage
                  src={currentTeam?.team_image}
                  alt={`${currentTeam?.name ?? "팀"} 이미지`}
                  size={18}
                />
                <span className="typo-subtitle5 max-w-24 truncate text-black">
                  {currentTeam?.name ?? "팀 명"}
                </span>
                <Icon
                  icon={IconChevronDown}
                  size={14}
                  className={`opacity-0 transition-all duration-200 group-hover:opacity-100 ${
                    isDropdownOpen ? "rotate-180 opacity-100" : ""
                  }`}
                />
              </button>
            ) : (
              <button
                type="button"
                onClick={() => setIsModalOpen(true)}
                className="flex cursor-pointer items-center gap-1.5 rounded-lg px-2 py-1 text-text-secondary transition-colors hover:bg-action-hover"
              >
                <Icon icon={IconAddPlus} size={16} className="shrink-0" />
                <span className="typo-subtitle5 whitespace-nowrap">
                  새 팀 만들기
                </span>
              </button>
            )}
            {isDropdownOpen && hasTeams && (
              <TeamListDropdown
                teams={teams}
                currentTeamId={currentTeam?.team_id ?? null}
                onSelectTeam={handleSelectTeam}
                onCreateTeam={handleCreateTeam}
                onInviteMember={handleInviteMember}
              />
            )}
          </>
        )}
      </div>
      {isModalOpen && (
        <CreateTeamModal
          onClose={() => setIsModalOpen(false)}
          onCreate={handleModalCreate}
          isPending={isPending}
        />
      )}
      {isInviteModalOpen && (
        <InviteTeamMemberModal
          onClose={() => setIsInviteModalOpen(false)}
          onInvite={invite}
          isPending={isInvitePending}
          errorMessage={inviteErrorMessage}
        />
      )}
    </>
  );
};
