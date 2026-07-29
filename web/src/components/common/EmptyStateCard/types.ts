export type EmptyStatePage = "Home" | "Decisions" | "Meetings" | "Git";

export interface EmptyStateCardProps {
  page: EmptyStatePage;
  onAction?: () => void;
}

export interface EmptyStateContent {
  title: string;
  description: string[];
  buttonLabel: string;
  footnote: string;
}
