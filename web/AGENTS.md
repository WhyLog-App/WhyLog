# Web 개발 가이드

## 이 폴더의 문서

- `AGENTS.md` — Web 개발 규칙과 참조 구현 경로 (이 문서)
- `README.md` — 로컬 실행·환경변수·경로 별칭 안내
- `biome.json` — 포매터·린터 설정 (규칙 변경은 회의 승격 후에만)
- 새 문서를 추가하거나 경로를 변경하면 이 목차와 루트 `AGENTS.md`의 파트 인덱스를 **함께** 갱신합니다.

## 기술 스택

| 항목 | 사용하는 것 | 비고 |
| --- | --- | --- |
| 언어·빌드 | TypeScript 5.9 / Vite 7 | `verbatimModuleSyntax` 켜짐 |
| UI | React 19 | 함수형 컴포넌트만 |
| 라우팅 | React Router 7 (`react-router-dom`) | `useRoutes` 기반 |
| 서버 상태 | TanStack Query 5 | 서버에서 온 데이터는 전부 여기 |
| HTTP | Axios (`@/utils/http`) | JWT 인터셉터 내장 |
| 스타일 | Tailwind CSS 4 | CSS Modules 사용 안 함 |
| 실시간 | `livekit-client` | 회의 화면 전용 |
| 검사 | Biome 2.4.7 | 포맷 + 린트 + import 정렬 |
| 패키지 | pnpm | `pnpm-lock.yaml` 커밋 필수 |

## 폴더 구조

```
web/src/
├── apis/            도메인별 HTTP 호출 함수 (auth.ts, teams.ts, meetings.ts, git.ts …)
├── assets/          이미지·아이콘
├── components/      화면을 가로지르는 공용 컴포넌트
│   └── {name}/
│       ├── index.tsx
│       ├── hooks/   이 컴포넌트에서만 쓰는 훅
│       └── types.ts 이 컴포넌트에서만 쓰는 타입
├── constants/       앱 전역 상수 (routes.ts 등)
├── hooks/           앱 전역 훅 / Provider (QueryContext, useCurrentTeam)
├── layout/          라우트 셸 (TeamLayout 등)
├── pages/           화면 단위. index.ts에서 배럴로 모아 export
│   └── {page}/
│       ├── index.tsx
│       ├── components/  이 페이지에서만 쓰는 컴포넌트
│       ├── hooks/       이 페이지에서만 쓰는 훅 (파일 1개 = 훅 1개)
│       ├── constants.ts 이 페이지의 정적 데이터·매직넘버
│       └── types.ts
├── router/          라우트 정의 (index.tsx)
├── styles/          app.css (Tailwind 진입점)
├── types/           도메인별 공용 타입 (auth.ts, decision.ts …)
└── utils/           http.ts, tokenStore.ts 등 순수 유틸
```

**배치 판단 기준 한 줄**: 두 군데 이상에서 쓰면 `src/` 상위로, 한 화면에서만 쓰면 그 화면 폴더 안에.

## 반드시 지킬 것

### 데이터 호출

- 서버 호출 함수는 **반드시** `src/apis/{도메인}.ts`에 둔다. 컴포넌트·훅 안에서 `http`를 직접 호출하지 않는다.
- 컴포넌트는 `apis/`를 직접 부르지 않는다. 항상 훅(`useQuery` / `useMutation`)을 한 겹 거친다.
- `useQuery`의 `queryKey`는 훅 파일에서 상수로 export 한다. 무효화하는 쪽이 같은 상수를 import 해서 쓴다.

  ```ts
  export const TEAMS_QUERY_KEY = ["teams"] as const;
  ```

- 변경 뮤테이션은 `onSuccess`에서 관련 `queryKey`를 `invalidateQueries` 한다.

### 에러 처리

- Axios 에러는 `isAxiosError`로 좁힌 뒤 `err.response?.data?.message ?? "기본 한국어 메시지"` 순서로 꺼낸다.
- 훅은 에러를 던지지 않는다. `errorMessage` 같은 상태로 돌려주고 화면이 표시한다.
- `catch`에서 조용히 삼키지 않는다. 최소한 `console.error`에 상황 설명 + 상태 코드를 남긴다.

### 타입

- 타입만 import 할 때는 `import type`을 쓴다 (`verbatimModuleSyntax` 때문에 안 그러면 빌드가 깨진다).
- `any` 금지. 모르는 값은 `unknown`으로 받고 좁힌다.
- API 응답 타입은 `src/types/{도메인}.ts`에 두고, 백엔드 스펙에 아직 없는 필드는 주석으로 `TODO: BE 스키마 미반영` 을 명시한다.

### 라우팅

- 경로 문자열을 화면에 직접 쓰지 않는다. `@/constants/routes`의 `ROUTES` / `createTeamRoute`를 쓴다.
- 라우트를 추가하면 `constants/routes.ts` → `router/index.tsx` → `pages/index.ts` 배럴, 세 곳을 같이 고친다.

### 상태 관리

- 서버에서 온 데이터는 전부 TanStack Query. `useState`로 복사해 두지 않는다.
- 화면 안에서만 쓰는 UI 상태만 `useState`. 여러 화면이 공유하면 Context(`src/hooks/`)로 올린다.
- 전역 상태 라이브러리를 새로 도입하지 않는다. 필요하면 회의 안건으로 올린다.

### 스타일링

- Tailwind 유틸리티 클래스만 쓴다. 인라인 `style`과 새 `.css` 파일은 만들지 않는다 (전역 리셋은 `styles/app.css`에서만).
- 색·간격은 임의값(`text-[#3b82f6]`) 대신 Tailwind 스케일을 쓴다. 정말 필요하면 그 이유를 주석으로 남긴다.
- 조건부 클래스는 템플릿 리터럴로 짧게. 3개 이상 분기면 컴포넌트를 쪼갠다.

### import 순서

Biome의 `organizeImports`가 자동 정렬한다. **손으로 순서를 바꾸지 않는다.** 저장 시 `pnpm check`가 정리해 준다. 다만 경로 형태는 아래를 지킨다.

1. 외부 패키지 (`react`, `axios`, `@tanstack/react-query`, `react-router-dom`)
2. 다른 폴더의 내부 모듈 — **`@/` 별칭 사용** (`@/apis/…`, `@/constants/…`, `@/utils/…`)
3. 같은 기능 폴더 안 — 상대 경로 (`./useMeetingList`, `../types`)

> 현재 `src/router/index.tsx` 등 일부 파일이 `../components/…` 형태로 남아 있다. 그 파일을 만질 일이 생기면 `@/`로 함께 고친다.

### 커밋 전

- `pnpm check` (포맷·린트·import 정렬 자동 수정) → `pnpm build` (타입 + 빌드) 둘 다 통과시키고 올린다.
- CI는 `pnpm exec biome ci .`로 **쓰기 없이** 같은 걸 다시 검사한다. 로컬에서 `check`를 안 돌리면 CI에서 막힌다.

## 참조 구현 경로

**`web/src/pages/meeting/`** — 새 화면을 만들 때 이 폴더를 그대로 따라간다.

이 화면 하나에 규칙이 전부 들어 있다.

| 볼 것 | 파일 |
| --- | --- |
| 목록 조회 + queryKey 상수 export | `pages/meeting/hooks/useMeetingList.ts` |
| 뮤테이션 + 캐시 무효화 + 에러 메시지 + 이동 | `pages/meeting/hooks/useDeleteMeeting.ts` |
| 호출 함수 분리 | `apis/meetings.ts` |
| 페이지 전용 컴포넌트·상수 배치 | `pages/meeting/components/`, `pages/meeting/constants.ts` |
| 라우트 등록 3곳 | `constants/routes.ts`, `router/index.tsx`, `pages/index.ts` |

참고로 `pages/landing/constants.ts`는 **정적 데이터를 컴포넌트에서 분리하는 방식**의 참조 구현이다.

## AI에게 시킬 때 주의점

1. **참조 구현 경로를 항상 같이 준다.** "`web/src/pages/meeting/`과 같은 구조로 만들어줘"를 프롬프트에 넣는다. 안 주면 매번 다른 폴더 구조가 나온다.
2. **작업 범위를 `web/` 안으로 못 박는다.** `server/`나 `ai/`를 같이 다른 파트를 건드려야 하면 코드를 고치지 말고 **PR 코멘트로 해당 가디언에게 설계만 질문**한다.
3. **`src/types/`와 `src/apis/`의 시그니처를 마음대로 바꾸지 말라고 명시한다.** 공용 인터페이스 변경은 적용 전에 먼저 제안하게 한다.
4. **라이브러리 추가 금지.** 상태 관리·폼·UI 킷을 새로 깔자는 제안이 나오면 코드로 반영하지 말고 회의 안건으로 올린다.
5. **`import type` 누락을 꼭 확인한다.**
6. **Tailwind 임의값 남발을 확인한다.**
