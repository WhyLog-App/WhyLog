import { footerMenuItems } from "../constants/menuItems";
import { useSidebarNavigation } from "../hooks/useSidebarNavigation";
import { MenuItem } from "./MenuItem";

interface SidebarFooterProps {
  isOpen: boolean;
}

export const SidebarFooter = ({ isOpen }: SidebarFooterProps) => {
  const { handleMenuClick, isActive } = useSidebarNavigation();

  return (
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
  );
};
