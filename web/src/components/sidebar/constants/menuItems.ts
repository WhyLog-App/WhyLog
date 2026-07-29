import IconNotebook from "@/assets/icons/file/ic_notebook.svg?react";
import IconLogout from "@/assets/icons/interface/ic_log_out.svg?react";
import IconSettings from "@/assets/icons/interface/ic_settings.svg?react";
import IconGit from "@/assets/icons/media/ic_git.svg?react";
import IconHeadphones from "@/assets/icons/media/ic_headphones.svg?react";
import IconHome from "@/assets/icons/media/ic_home.svg?react";
import type { MenuItem } from "../types";

export const mainMenuItems: MenuItem[] = [
  { id: "home", icon: IconHome, label: "홈", path: "/" },
];

export const subMenuItems: MenuItem[] = [
  {
    id: "decisions",
    icon: IconNotebook,
    label: "결정 사항",
    path: "/decisions",
  },
  { id: "meeting", icon: IconHeadphones, label: "음성 회의", path: "/meeting" },
  { id: "git", icon: IconGit, label: "GIT", path: "/git" },
];

export const footerMenuItems: MenuItem[] = [
  { id: "settings", icon: IconSettings, label: "설정", path: "/settings" },
  { id: "logout", icon: IconLogout, label: "로그아웃" },
];
