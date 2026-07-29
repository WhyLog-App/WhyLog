import { mainMenuItems, subMenuItems } from "../constants/menuItems";
import { useSidebarNavigation } from "../hooks/useSidebarNavigation";
import { MenuItem } from "./MenuItem";

interface SidebarNavProps {
  isOpen: boolean;
}

export const SidebarNav = ({ isOpen }: SidebarNavProps) => {
  const { handleMenuClick, isActive } = useSidebarNavigation();

  return (
    <nav className="flex flex-1 flex-col gap-2">
      {mainMenuItems.map((item) => (
        <MenuItem
          key={item.id}
          item={item}
          isOpen={isOpen}
          isActive={isActive(item)}
          onClick={handleMenuClick}
        />
      ))}

      <div className="h-px w-full bg-border-divider" />

      {subMenuItems.map((item) => (
        <MenuItem
          key={item.id}
          item={item}
          isOpen={isOpen}
          isActive={isActive(item)}
          onClick={handleMenuClick}
        />
      ))}
    </nav>
  );
};
