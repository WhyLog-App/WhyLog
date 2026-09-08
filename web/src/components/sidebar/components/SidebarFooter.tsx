import Modal from "@/components/common/Modal";
import { footerMenuItems } from "../constants/menuItems";
import { useSidebarNavigation } from "../hooks/useSidebarNavigation";
import { MenuItem } from "./MenuItem";

interface SidebarFooterProps {
  isOpen: boolean;
}

export const SidebarFooter = ({ isOpen }: SidebarFooterProps) => {
  const {
    handleMenuClick,
    isActive,
    isLogoutModalOpen,
    isLoggingOut,
    closeLogoutModal,
    confirmLogout,
    logoutWarningMessage,
  } = useSidebarNavigation();

  return (
    <>
      <div className="flex flex-col gap-2">
        {footerMenuItems.map((item) => (
          <MenuItem
            key={item.id}
            item={item}
            isOpen={isOpen}
            isActive={isActive(item)}
            onClick={handleMenuClick}
          />
        ))}
      </div>
      {isLogoutModalOpen && (
        <Modal
          title="로그아웃"
          onClose={closeLogoutModal}
          primaryLabel={
            logoutWarningMessage
              ? "다시 시도"
              : isLoggingOut
                ? "로그아웃 중..."
                : "로그아웃"
          }
          onPrimaryClick={confirmLogout}
          isPrimaryDisabled={isLoggingOut}
        >
          <p className="typo-body4 text-(--color-text-secondary)">
            {logoutWarningMessage ?? "현재 계정에서 로그아웃하시겠습니까?"}
          </p>
        </Modal>
      )}
    </>
  );
};
