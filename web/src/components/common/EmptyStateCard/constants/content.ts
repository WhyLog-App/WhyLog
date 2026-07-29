import type { EmptyStateContent, EmptyStatePage } from "../types";

export const CONTENT: Record<EmptyStatePage, EmptyStateContent> = {
  Home: {
    title: "WhyLog에 오신 걸 환영해요",
    description: [
      "새로운 팀을 만들어 코드 변경의 맥락을 기록해보세요.",
      "기존 팀이 있다면 팀원의 초대를 통해 자동으로 참여됩니다.",
    ],
    buttonLabel: "새 팀 만들기",
    footnote:
      "팀에 참여하면 결정사항, 회의 분석, Git 연동 기능을 사용할 수 있어요",
  },
  Decisions: {
    title: '코드 변경의 "Why"를 기록해보세요',
    description: [
      "회의를 진행하면 AI가 결정사항을 자동 추출하고,",
      "Git 커밋과 연결하여 의사결정의 맥락을 보존합니다.",
    ],
    buttonLabel: "회의 시작하기",
    footnote: "결정사항은 회의 분석을 통해 자동 생성됩니다",
  },
  Meetings: {
    title: "회의를 기록하고 분석해보세요",
    description: [
      "음성 회의를 진행하면",
      "AI가 회의 맥락을 분석하여 핵심 결정사항을 자동으로 추출합니다.",
    ],
    buttonLabel: "회의 시작하기",
    footnote: "회의 중 합의된 기술 결정, 구조 변경 등이 자동 추출됩니다",
  },
  Git: {
    title: "GitHub 레포지토리를 연동해주세요",
    description: [
      "레포지토리를 연동하면 커밋 이력을 수집하고,",
      "회의에서 나온 결정사항과 자동으로 연결할 수 있습니다.",
    ],
    buttonLabel: "레포지토리 연동하기",
    footnote: "현재 GitHub 레포지토리 연동만 지원됩니다",
  },
};
