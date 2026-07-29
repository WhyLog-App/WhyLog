import type { IconSource } from "@/components/common/Icon";

export interface MenuItem {
  id: string;
  icon: IconSource;
  label: string;
  path?: string;
}
