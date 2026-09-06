import type {
  RelationshipMapEdge,
  RelationshipMapNode,
} from "./relationshipMap.types";

// TODO: BE 스키마 미반영. 관계도 API가 확정되면 nodes/edges 형태로 변환한다.
export const RELATIONSHIP_MAP_NODES: RelationshipMapNode[] = [
  {
    id: "column-context",
    type: "relationshipColumn",
    position: { x: 0, y: 0 },
    draggable: false,
    selectable: false,
    data: { title: "원문 맥락", description: "회의 발언" },
  },
  {
    id: "column-reason",
    type: "relationshipColumn",
    position: { x: 360, y: 0 },
    draggable: false,
    selectable: false,
    data: { title: "결정 근거", description: "합의에 영향을 준 이유" },
  },
  {
    id: "column-decision",
    type: "relationshipColumn",
    position: { x: 720, y: 0 },
    draggable: false,
    selectable: false,
    data: { title: "결정 사항", description: "회의에서 확정한 내용" },
  },
  {
    id: "column-application",
    type: "relationshipColumn",
    position: { x: 1080, y: 0 },
    draggable: false,
    selectable: false,
    data: { title: "코드 반영", description: "결정과 연결된 커밋" },
  },
  {
    id: "context-ttl",
    type: "relationshipCard",
    position: { x: 0, y: 92 },
    data: {
      kind: "context",
      title: "세션 유형별 TTL 분리 필요",
      description: "인증 세션과 캐시 세션이 같은 만료 시간을 사용하고 있어요.",
      meta: "김준용 · 12:24",
    },
  },
  {
    id: "context-mongo",
    type: "relationshipCard",
    position: { x: 0, y: 308 },
    data: {
      kind: "context",
      title: "Mongo 이전 대신 Redis 유지 제안",
      description:
        "운영 복잡도를 높이지 않고 키 네이밍 규칙을 바꾸는 편이 낫습니다.",
      meta: "유진 · 12:26",
    },
  },
  {
    id: "context-memory",
    type: "relationshipCard",
    position: { x: 0, y: 524 },
    data: {
      kind: "context",
      title: "키 정책 분리로 메모리 비용 대응",
      description:
        "세션 특성에 따라 정책을 나누면 메모리 이슈를 해결할 수 있습니다.",
      meta: "유상완 · 12:27",
    },
  },
  {
    id: "reason-operation",
    type: "relationshipCard",
    position: { x: 360, y: 186 },
    data: {
      kind: "reason",
      title: "운영 복잡도 증가 우려",
      description: "저장소를 변경하면 마이그레이션과 운영 부담이 커집니다.",
      meta: "근거 01",
    },
  },
  {
    id: "reason-memory",
    type: "relationshipCard",
    position: { x: 360, y: 458 },
    data: {
      kind: "reason",
      title: "Redis 메모리 비용 관리",
      description: "세션 성격에 맞는 TTL 정책이 비용과 성능의 균형을 맞춥니다.",
      meta: "근거 02",
    },
  },
  {
    id: "decision-redis-policy",
    type: "relationshipCard",
    position: { x: 720, y: 304 },
    data: {
      kind: "decision",
      title: "Redis 유지 및 세션별 키 정책 분리",
      description:
        "저장소는 유지하고 인증·캐시 세션의 TTL과 키 규칙을 분리합니다.",
      meta: "확정 · 12:28",
    },
  },
  {
    id: "application-session-store",
    type: "relationshipCard",
    position: { x: 1080, y: 170 },
    data: {
      kind: "application",
      title: "세션 저장소 인터페이스 분리",
      description:
        "feat: 세션별 정책을 주입할 수 있도록 저장소 계층을 분리했습니다.",
      meta: "WhyLog-Backend · a13f9c2",
    },
  },
  {
    id: "application-key-policy",
    type: "relationshipCard",
    position: { x: 1080, y: 458 },
    data: {
      kind: "application",
      title: "세션 키 정책 적용",
      description: "feat: 인증·캐시 세션의 TTL 및 키 접두사를 분리했습니다.",
      meta: "WhyLog-Backend · b8fd9ad",
    },
  },
  // 독립된 결정 묶음: 다른 결정과 선택 강조가 섞이지 않는지 확인한다.
  {
    id: "context-api-contract",
    type: "relationshipCard",
    position: { x: 0, y: 760 },
    data: {
      kind: "context",
      title: "클라이언트가 오류 응답을 예측할 수 없음",
      description:
        "성공·실패 응답의 필드 형태가 달라 화면별 예외 처리가 늘고 있습니다.",
      meta: "수빈 · 14:02",
    },
  },
  {
    id: "context-error-observability",
    type: "relationshipCard",
    position: { x: 0, y: 980 },
    data: {
      kind: "context",
      title: "오류 원인을 추적하기 어려움",
      description:
        "상태 코드와 오류 코드가 일관되지 않아 운영 로그 분석이 지연됩니다.",
      meta: "도현 · 14:05",
    },
  },
  {
    id: "reason-api-contract",
    type: "relationshipCard",
    position: { x: 360, y: 760 },
    data: {
      kind: "reason",
      title: "클라이언트 호환성 확보",
      description:
        "모든 API가 같은 응답 계약을 제공해야 화면 구현을 단순화할 수 있습니다.",
      meta: "근거 03",
    },
  },
  {
    id: "reason-error-observability",
    type: "relationshipCard",
    position: { x: 360, y: 980 },
    data: {
      kind: "reason",
      title: "운영 오류 추적성 개선",
      description:
        "표준화된 오류 코드가 있어야 장애 원인을 빠르게 분류할 수 있습니다.",
      meta: "근거 04",
    },
  },
  {
    id: "decision-api-contract",
    type: "relationshipCard",
    position: { x: 720, y: 870 },
    data: {
      kind: "decision",
      title: "공통 API 응답 계약 도입",
      description: "성공·실패 응답과 오류 코드를 공통 포맷으로 통일합니다.",
      meta: "확정 · 14:12",
    },
  },
  {
    id: "application-web-error-handler",
    type: "relationshipCard",
    position: { x: 1080, y: 760 },
    data: {
      kind: "application",
      title: "웹 오류 처리 공통화",
      description:
        "feat: 공통 오류 응답을 해석하는 클라이언트 핸들러를 추가했습니다.",
      meta: "WhyLog-Web · c12de34",
    },
  },
  {
    id: "application-server-response",
    type: "relationshipCard",
    position: { x: 1080, y: 980 },
    data: {
      kind: "application",
      title: "서버 응답 래퍼 적용",
      description:
        "feat: API 성공·실패 응답을 공통 래퍼로 반환하도록 변경했습니다.",
      meta: "WhyLog-Server · e56fa78",
    },
  },
  // 코드 반영까지 이어지는 단일 연결 경로다.
  {
    id: "context-migration",
    type: "relationshipCard",
    position: { x: 0, y: 1200 },
    data: {
      kind: "context",
      title: "무중단 마이그레이션 필요",
      description:
        "사용자 요청이 진행되는 시간에도 데이터 스키마를 안전하게 변경해야 합니다.",
      meta: "준용 · 15:01",
    },
  },
  {
    id: "reason-zero-downtime",
    type: "relationshipCard",
    position: { x: 360, y: 1200 },
    data: {
      kind: "reason",
      title: "배포 중 서비스 중단 방지",
      description: "점진 전환과 되돌리기 가능한 절차가 필요합니다.",
      meta: "근거 05",
    },
  },
  {
    id: "decision-expand-contract",
    type: "relationshipCard",
    position: { x: 720, y: 1200 },
    data: {
      kind: "decision",
      title: "Expand-Contract 방식으로 마이그레이션",
      description:
        "호환 필드를 먼저 추가하고, 전환 완료 후 이전 필드를 제거합니다.",
      meta: "확정 · 15:08",
    },
  },
  {
    id: "application-migration-script",
    type: "relationshipCard",
    position: { x: 1080, y: 1200 },
    data: {
      kind: "application",
      title: "호환 마이그레이션 스크립트",
      description:
        "feat: 단계별 데이터 전환과 검증을 수행하는 배치 스크립트를 추가했습니다.",
      meta: "WhyLog-Server · f90ab12",
    },
  },
  // 결정은 확정됐지만 아직 코드 반영이 없는 경우다.
  {
    id: "context-dependency-review",
    type: "relationshipCard",
    position: { x: 0, y: 1420 },
    data: {
      kind: "context",
      title: "외부 의존성의 보안 패치 필요",
      description:
        "취약점 공지가 있었지만 호환성 검증이 아직 끝나지 않았습니다.",
      meta: "윤지 · 16:10",
    },
  },
  {
    id: "reason-dependency-review",
    type: "relationshipCard",
    position: { x: 360, y: 1420 },
    data: {
      kind: "reason",
      title: "호환성 검증 후 일괄 업데이트",
      description:
        "서비스 영향도를 확인한 뒤 다음 배포 주기에 반영하는 편이 안전합니다.",
      meta: "근거 06",
    },
  },
  {
    id: "decision-dependency-update",
    type: "relationshipCard",
    position: { x: 720, y: 1420 },
    data: {
      kind: "decision",
      title: "다음 배포 주기에 의존성 업데이트",
      description: "검증 환경에서 확인을 마친 뒤 보안 패치를 함께 반영합니다.",
      meta: "확정 · 16:16",
    },
  },
  // 연결 관계가 아직 판별되지 않은 발언도 표시할 수 있다.
  {
    id: "context-unlinked",
    type: "relationshipCard",
    position: { x: 0, y: 1640 },
    data: {
      kind: "context",
      title: "모니터링 대시보드 개편 제안",
      description:
        "회의에서 제안됐지만 이번 회의의 결정사항에는 아직 연결되지 않았습니다.",
      meta: "상완 · 16:23",
    },
  },
];

export const RELATIONSHIP_MAP_EDGES: RelationshipMapEdge[] = [
  {
    id: "context-ttl-reason-memory",
    source: "context-ttl",
    target: "reason-memory",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "context-mongo-reason-operation",
    source: "context-mongo",
    target: "reason-operation",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "context-memory-reason-memory",
    source: "context-memory",
    target: "reason-memory",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "reason-operation-decision",
    source: "reason-operation",
    target: "decision-redis-policy",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
  {
    id: "reason-memory-decision",
    source: "reason-memory",
    target: "decision-redis-policy",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
  {
    id: "decision-session-store",
    source: "decision-redis-policy",
    target: "application-session-store",
    type: "smoothstep",
    data: { relation: "코드 반영" },
  },
  {
    id: "decision-key-policy",
    source: "decision-redis-policy",
    target: "application-key-policy",
    type: "smoothstep",
    data: { relation: "코드 반영" },
  },
  {
    id: "context-api-contract-reason",
    source: "context-api-contract",
    target: "reason-api-contract",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "context-error-observability-reason",
    source: "context-error-observability",
    target: "reason-error-observability",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "reason-api-contract-decision",
    source: "reason-api-contract",
    target: "decision-api-contract",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
  {
    id: "reason-error-observability-decision",
    source: "reason-error-observability",
    target: "decision-api-contract",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
  {
    id: "decision-api-contract-web",
    source: "decision-api-contract",
    target: "application-web-error-handler",
    type: "smoothstep",
    data: { relation: "코드 반영" },
  },
  {
    id: "decision-api-contract-server",
    source: "decision-api-contract",
    target: "application-server-response",
    type: "smoothstep",
    data: { relation: "코드 반영" },
  },
  {
    id: "context-migration-reason",
    source: "context-migration",
    target: "reason-zero-downtime",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "reason-zero-downtime-decision",
    source: "reason-zero-downtime",
    target: "decision-expand-contract",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
  {
    id: "decision-expand-contract-application",
    source: "decision-expand-contract",
    target: "application-migration-script",
    type: "smoothstep",
    data: { relation: "코드 반영" },
  },
  {
    id: "context-dependency-review-reason",
    source: "context-dependency-review",
    target: "reason-dependency-review",
    type: "smoothstep",
    data: { relation: "근거가 된 발언" },
  },
  {
    id: "reason-dependency-review-decision",
    source: "reason-dependency-review",
    target: "decision-dependency-update",
    type: "smoothstep",
    data: { relation: "결정 근거" },
  },
];
