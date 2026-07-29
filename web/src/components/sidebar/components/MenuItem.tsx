import { Icon } from "@/components/common/Icon";
import type { MenuItem as MenuItemType } from "../types";

interface MenuItemProps {
  item: MenuItemType;
  isOpen: boolean;
  isActive: boolean;
  onClick: (item: MenuItemType) => void;
}

export const MenuItem = ({
  item,
  isOpen,
  isActive,
  onClick,
}: MenuItemProps) => {
  return (
    <button
      type="button"
      onClick={() => onClick(item)}
      className={`flex cursor-pointer items-center gap-3 rounded-lg p-3 transition-colors
        ${isOpen ? "w-50" : "size-12 justify-center"}
        ${isActive ? "bg-action-active" : "bg-bg-surface hover:bg-action-hover"}
        ${isActive ? "text-text-brand-darker" : "text-text-secondary"}
      `}
    >
      <Icon icon={item.icon} size={24} className="shrink-0" />
      {isOpen && (
        <span className="typo-legend flex-1 text-left tracking-tight">
          {item.label}
        </span>
      )}
    </button>
  );
};
