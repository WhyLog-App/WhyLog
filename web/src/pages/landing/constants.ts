/**
 * 랜딩 페이지 정적 데이터 & 매직넘버.
 * - 모든 섹션의 카드/리스트 데이터는 여기에서 import한다.
 * - 컴포넌트는 props 없이 정적 렌더링되므로 데이터는 모듈 상수로 보관.
 */

export const HERO_IMAGE = "/assets/landing/iridescent-flow.png";

// ---------- Hero ----------

export const HERO_TYPEWRITER_SPEED = 80;
export const HERO_TYPEWRITER_START_DELAY = 300;

// ---------- Starfield / Waveform ----------

export const HERO_STAR_COUNT = 42;

export const WAVEFORM_BARS = [
  30, 70, 90, 50, 65, 40, 80, 95, 55, 75, 35, 60, 88, 48, 70, 32, 82, 58, 42,
  68, 80, 45, 62, 38, 74, 92, 50, 33,
] as const;

// ---------- Problem section ----------

export type ProblemCard = {
  id: string;
  n: string;
  title: string;
  body: string;
};

export const PROBLEM_CARDS: ProblemCard[] = [
  {
    id: "ctx-loss",
    n: "01",
    title: '"왜 이렇게 짰지?"',
    body: "3개월 전 결정한 구조인데, 그때 이유가 도무지 기억나지 않습니다.",
  },
  {
    id: "minutes-search",
    n: "02",
    title: '"회의록을 뒤져도"',
    body: "어떤 대안이 논의됐고, 왜 기각됐는지 다시 찾아내기 어렵습니다.",
  },
  {
    id: "git-what",
    n: "03",
    title: '"Git에는 결과만"',
    body: '커밋 메시지는 "무엇을" 했는지만 남길 뿐, "왜"는 비어 있습니다.',
  },
];

// ---------- Features section ----------

export const FEATURE_1_BULLETS = [
  "실시간 음성 → 텍스트 전사",
  "주제 / 맥락 / 결정 자동 분리",
  "발화자 식별 및 신뢰도 점수",
] as const;

export const FEATURE_2_BULLETS = [
  "발화 단위로 묶인 결정 근거",
  "참여자 합의도 자동 산출",
  "구조화된 Decision Log 보관",
] as const;

export const FEATURE_3_BULLETS = [
  "Hybrid Retrieval 기반 추천",
  "커밋별 매칭 사유 제공",
  "결정 ↔ 구현 일치율 모니터링",
] as const;

export type TimelineRow = { t: string; label: string; strong: boolean };

export const FEATURE_2_TIMELINE: TimelineRow[] = [
  { t: "12:24", label: "장애 이슈 제기", strong: false },
  { t: "12:26", label: "Redis vs Mongo 논의", strong: false },
  { t: "12:28", label: "최종 결정", strong: true },
];

export type CommitRow = { repo: string; sha: string; msg: string };

export const FEATURE_3_COMMITS: CommitRow[] = [
  { repo: "WhyLog-Backend", sha: "a13f9c2", msg: "feat: 마이페이지 API 구현" },
  {
    repo: "WhyLog-Frontend",
    sha: "e7c2b1a",
    msg: "feat: 유저 프로필 컴포넌트",
  },
  { repo: "WhyLog-Backend", sha: "9b3da42", msg: "refactor: 세션 저장소 분리" },
];

// ---------- How It Works section ----------

export type HowStep = {
  id: number;
  n: number;
  title: string;
  body: string;
  offset: boolean;
};

export const HOW_STEPS: HowStep[] = [
  {
    id: 1,
    n: 1,
    title: "회의 시작",
    body: "음성 회의를 시작하면 자동으로 녹음·전사됩니다.",
    offset: false,
  },
  {
    id: 2,
    n: 2,
    title: "AI 분석",
    body: "논의 주제, 결정 근거, 최종 결정을 AI가 추출합니다.",
    offset: true,
  },
  {
    id: 3,
    n: 3,
    title: "결정사항 저장",
    body: "신뢰도 점수와 함께 구조화된 Decision Log로 보존됩니다.",
    offset: false,
  },
  {
    id: 4,
    n: 4,
    title: "Git 커밋 매칭",
    body: "Hybrid Retrieval로 관련 커밋을 자동 추천하고 연결합니다.",
    offset: true,
  },
];

// ---------- Tech section ----------

export type AccuracyBar = { id: string; cls: string; h: number };

export const TECH_ACCURACY_BARS: AccuracyBar[] = [
  { id: "b1", cls: "bg-primary-100", h: 30 },
  { id: "b2", cls: "bg-primary-200", h: 50 },
  { id: "b3", cls: "bg-primary-300", h: 65 },
  { id: "b4", cls: "bg-primary-400", h: 80 },
  { id: "b5", cls: "bg-primary-500", h: 100 },
];

// ---------- Use Cases section ----------

export type UseCase = {
  id: string;
  tag: string;
  gradient: string;
  shadow: string;
  kicker: string;
  title: string;
  body: string;
  statPrefix?: string;
  statValue: string;
  statSuffix?: string;
};

export const USE_CASES: UseCase[] = [
  {
    id: "be",
    tag: "BE",
    gradient: "linear-gradient(135deg, #1e5be8, #589efa)",
    shadow: "0 10px 24px rgba(30,91,232,0.3)",
    kicker: "백엔드 팀",
    title: "아키텍처 결정의 추적",
    body: "스프린트 회의에서 정한 아키텍처 결정을 6개월 후에도 그대로 추적합니다.",
    statValue: "42",
    statSuffix: "건의 결정 / 분기",
  },
  {
    id: "fe",
    tag: "FE",
    gradient: "linear-gradient(135deg, #a78bfa, #f0abfc)",
    shadow: "0 10px 24px rgba(167,139,250,0.35)",
    kicker: "프론트엔드 팀",
    title: "디자인 시스템 변경 추적",
    body: "디자인 시스템 변경 논의의 맥락을 컴포넌트 PR과 자연스럽게 연결합니다.",
    statValue: "128",
    statSuffix: "개의 PR 매칭",
  },
  {
    id: "su",
    tag: "SU",
    gradient: "linear-gradient(135deg, #7ab7ff, #1e5be8)",
    shadow: "0 10px 24px rgba(30,91,232,0.3)",
    kicker: "스타트업",
    title: "신규 입사자 온보딩",
    body: "새로 합류한 동료가 코드 히스토리의 'Why'를 며칠이 아닌 시간 단위로 학습합니다.",
    statPrefix: "온보딩 시간 ",
    statValue: "−63%",
  },
];

// ---------- FAQ section ----------

export type FaqItem = { id: string; q: string; a: string; open: boolean };

export const FAQ_ITEMS: FaqItem[] = [
  {
    id: "tools",
    q: "어떤 회의 도구와 연동되나요?",
    a: "자체 음성 회의 기능을 기본 제공합니다. Zoom, Google Meet 연동은 베타 단계로 곧 공개될 예정입니다.",
    open: true,
  },
  {
    id: "git",
    q: "어떤 Git 플랫폼을 지원하나요?",
    a: "현재 GitHub 레포지토리 연동을 지원합니다. GitLab, Bitbucket 연동은 로드맵에 포함되어 있습니다.",
    open: false,
  },
  {
    id: "security",
    q: "회의 데이터는 안전한가요?",
    a: "모든 데이터는 전송·저장 시 암호화되며, 팀 단위 접근 제어로만 접근할 수 있습니다. SOC2 준수를 진행 중입니다.",
    open: false,
  },
  {
    id: "pricing",
    q: "무료로 사용할 수 있나요?",
    a: "5명 이하 소규모 팀은 무료로 시작할 수 있습니다. 카드 등록 없이 바로 사용할 수 있습니다.",
    open: false,
  },
];

// ---------- Footer ----------

export type FooterColumn = {
  id: string;
  title: string;
  items: string[];
  start: string;
};

export const FOOTER_COLUMNS: FooterColumn[] = [
  {
    id: "product",
    title: "제품",
    items: ["기능", "가격", "변경사항", "로드맵"],
    start: "md:col-start-6",
  },
  {
    id: "resources",
    title: "리소스",
    items: ["문서", "블로그", "API", "사용 가이드"],
    start: "",
  },
  {
    id: "company",
    title: "회사",
    items: ["팀 소개", "채용", "문의", "이용약관"],
    start: "",
  },
];

export const CONTACT_EMAIL = "whylog1234@gmail.com";

// ---------- Scroll progress thresholds ----------

/** how-section: progress 시작 — 섹션 상단이 뷰포트 85% 지점에 닿을 때 0. */
export const HOW_PROGRESS_START_RATIO = 0.85;
/** how-section: progress 종료 — 섹션 하단이 뷰포트 50% 지점에 닿을 때 1. */
export const HOW_PROGRESS_END_RATIO = 0.5;
