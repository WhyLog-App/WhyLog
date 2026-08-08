# AI 개발 가이드

## 이 폴더의 문서

- `AGENTS.md` — AI 파트의 개발 규칙과 참조 구현 경로
- [`README.md`](README.md) — 로컬 환경 구성, 서버 실행, 패키지 추가, Docker 사용법
- [`docs/commit_matching_evaluation.md`](docs/commit_matching_evaluation.md) — 적용사항-커밋 매칭 평가 방법과 지표
- 새 문서를 추가하거나 경로를 변경하면 이 목차와 루트 `AGENTS.md`의 파트 인덱스를 함께 갱신합니다.

아래 경로 표기는 모두 `ai/`를 기준으로 합니다.

## 기술 스택

- Python 3.13 이상
- FastAPI 0.116.1, Pydantic
- Google Gemini (`google-genai`) — 텍스트 생성과 임베딩
- Deepgram — 음성 전사
- ChromaDB — 커밋·적용사항 임베딩 저장 및 검색
- uv — 가상환경과 의존성 관리
- Ruff — 린트와 포맷
- pytest, pytest-asyncio — 테스트

의존성 버전의 정본은 `pyproject.toml`과 `uv.lock`입니다. 패키지는 파일을 직접 수정하지 말고 `uv add` 또는 `uv add --dev`로 추가하고 `uv.lock`을 함께 커밋합니다.

## 폴더 구조

- `app/main.py` — FastAPI 앱 생성, 미들웨어, 공통 예외 처리
- `app/api/router.py` — 도메인 라우터 조립
- `app/core/` — 설정, 공통 응답·예외·enum, Gemini 재시도, ChromaDB 클라이언트
- `app/domains/{domain}/router.py` — HTTP 엔드포인트와 응답 조립
- `app/domains/{domain}/schemas.py` — 요청·응답 Pydantic 모델
- `app/domains/{domain}/services/` — 도메인 로직, 외부 AI API 호출, 파싱·검증
- `tests/` — 동작 및 회귀 테스트
- `tests/fixtures/` — 평가·테스트용 고정 입력 데이터
- `scripts/` — 운영 API와 분리된 평가·관리 스크립트
- `docs/` — AI 파트의 설계·평가 문서

현재 애플리케이션은 `app/` 구조를 사용합니다. 루트의 `routers/`, `schemas/`, `services/`, `utils/`는 현재 실행 경로에서 import되지 않는 이전 구현입니다. 새 기능은 `app/` 아래에 작성하고, 이전 구현의 삭제는 기능 이관 여부와 테스트를 확인한 뒤 별도 PR로 처리합니다.

## 반드시 지킬 것

### 계층과 책임

- 호출 방향은 `router → service → core 또는 외부 SDK`로 유지합니다.
- router는 입력 전달, 서비스 호출, 공통 응답 래핑만 담당합니다. 프롬프트 작성, LLM 응답 파싱, 매칭 점수 계산을 router에 두지 않습니다.
- 요청·응답 계약은 해당 도메인의 `schemas.py`에 Pydantic 모델로 정의합니다. 검증되지 않은 `dict`를 API 응답으로 직접 반환하지 않습니다.
- 여러 도메인에서 공유하는 기반 기능만 `app/core/`에 둡니다. 특정 도메인의 규칙과 프롬프트를 `core`로 옮기지 않습니다.
- 비동기 엔드포인트에서 동기 네트워크 호출로 이벤트 루프를 막지 않습니다. 외부 SDK의 async API를 우선 사용합니다.

### LLM 호출 규약

- Gemini 텍스트 생성은 `app/core/gemini.py`의 `generate_content_with_retry`를 사용합니다. 서비스에서 `client.aio.models.generate_content`를 직접 호출해 공통 timeout·retry·usage 로그를 우회하지 않습니다.
- 모든 LLM 호출에 `timeout`, 식별 가능한 `operation_name`, 원문을 포함하지 않은 `log_context`를 전달합니다.
- 재시도는 공통 함수가 처리하는 429·5xx·timeout에만 적용합니다. 인증 오류, 잘못된 요청, 파싱 오류를 무조건 재시도하지 않습니다.
- 모델명과 API 키는 `app/core/config.py`의 `settings`를 사용합니다. 서비스 코드에 모델명이나 비밀값을 하드코딩하지 않습니다.
- LLM이 JSON을 반환해야 하는 호출은 `response_mime_type="application/json"`을 지정하고, `json.loads` 이후 Pydantic 모델로 다시 검증합니다.
- LLM 출력은 신뢰하지 않습니다. 필수 필드, enum, 시간 순서, 점수 범위와 식별자 대응 관계를 코드에서 검증한 뒤 사용합니다.
- 프롬프트·회의 원문·전사 원문·diff·API 키를 로그에 남기지 않습니다. 사용량, 모델명, 처리 건수, 식별자처럼 진단에 필요한 메타데이터만 기록합니다.

### 프롬프트 관리

- 프롬프트는 해당 기능을 소유한 `app/domains/{domain}/services/`에 상수 또는 전용 빌더 함수로 둡니다.
- 같은 프롬프트 문자열을 router, 테스트, 여러 서비스에 복사하지 않습니다.
- 시스템 규칙과 사용자 입력을 구분하고, 사용자 입력을 규칙 문자열에 삽입할 때 입력 경계를 명확히 표시합니다.
- 프롬프트를 변경하면 출력 스키마, 파서, 관련 테스트를 함께 확인합니다. 커밋 매칭 결과에 영향을 주는 변경은 `tests/fixtures/commit_matching_golden_cases.json` 평가도 실행합니다.
- 모델명, temperature, timeout, 출력 형식 변경은 동작 변경으로 취급하고 PR 본문에 변경 이유와 검증 결과를 남깁니다.
- 프롬프트 전문을 환경변수나 저장소 밖 문서에만 두지 않습니다. 코드 동작을 결정하는 프롬프트는 코드와 함께 버전 관리합니다.

### 임베딩과 ChromaDB

- ChromaDB 클라이언트와 컬렉션 생성은 `app/core/chroma.py`를 통합니다. 서비스마다 새 `PersistentClient`를 만들지 않습니다.
- 컬렉션 이름과 임베딩 모델은 `settings`에서 가져옵니다.
- 저장 문서 ID와 metadata 필드를 변경하면 기존 데이터 및 Server 연동 호환성을 검토합니다. 호환되지 않는 변경은 마이그레이션 계획 없이 적용하지 않습니다.
- 유사도 임계값, 가중치, 감점 규칙을 변경하면 근거와 전후 평가 결과를 남깁니다. 특정 예시 하나만 통과시키기 위해 상수를 조정하지 않습니다.
- 테스트가 로컬 ChromaDB 상태에 의존하지 않도록 클라이언트 싱글턴과 컬렉션 상태를 격리합니다.

### 응답과 예외

- 성공 응답은 `app/core/responses.py`의 `ok_response`로 감싸 `ApiResponse[T]` 형식을 유지합니다.
- 서비스에서 예상 가능한 오류는 `AppServiceError`로 전달하고, router나 서비스에서 제각각 오류 응답 JSON을 만들지 않습니다.
- 외부 API 오류를 그대로 사용자에게 노출하지 않습니다. 인증정보, 원문 요청, SDK 내부 메시지를 숨기고 서비스 공통 메시지로 변환합니다.
- 예외를 포괄적으로 잡아 정상 결과처럼 반환할 때는 해당 기능에 명시된 fallback 정책이 있어야 하며, 반드시 구조화 로그를 남깁니다.

### 설정과 보안

- 비밀값은 `.env` 또는 배포 환경변수로 주입합니다. `.env`, API 키, 토큰, 실제 회의·전사 데이터는 커밋하지 않습니다.
- 환경변수는 `app/core/config.py`에서 읽고 서비스 곳곳에서 `os.getenv`를 새로 추가하지 않습니다. 기존 직접 조회는 관련 코드를 수정할 때 `settings`로 모읍니다.
- 새 설정값에는 타입, 안전한 기본값 여부, 누락 시 동작을 명확히 정의합니다. 필수 비밀값에 가짜 운영 기본값을 두지 않습니다.

### 코드 작성 공통

- Python 3.13 실행 환경을 기준으로 하며, 공개 함수와 데이터 구조에는 타입 힌트를 작성합니다.
- 포맷과 린트 기준은 `pyproject.toml`의 Ruff 설정을 따릅니다. 검사를 통과시키려고 규칙을 무분별하게 `noqa`로 억제하지 않습니다.
- 실패하는 테스트를 삭제하거나 비활성화해 CI를 통과시키지 않습니다.
- 네트워크가 필요한 테스트는 실제 외부 API를 호출하지 않고 클라이언트 경계를 mock 또는 fake로 대체합니다.
- 주석은 이름과 구조만으로 의도가 드러나지 않을 때만 작성합니다.

## 테스트와 검증

가장 작은 검증부터 실행하고, 변경 범위에 따라 넓힙니다. 명령은 `ai/` 폴더에서 실행합니다.

```bash
uv run ruff check .
uv run ruff format --check .
uv run pytest -q
```

커밋 매칭 로직이나 프롬프트를 변경한 경우 평가 스크립트도 실행합니다.

```bash
uv run python scripts/evaluate_commit_matching.py \
  --response /path/to/commit-match-response.json \
  --fail-on-failure
```

API 실행 확인이 필요하면 아래 명령으로 서버를 띄우고 `/health`를 확인합니다.

```bash
uv run uvicorn app.main:app --reload
```

문서만 변경한 경우에는 링크와 diff 확인으로 갈음할 수 있습니다.

## 경계

아래 변경은 Server 계약, 저장 데이터 또는 운영 비용에 영향을 주므로 담당자 확인 없이 바로 적용하지 않습니다. AI에게 시킬 때도 구현보다 제안을 먼저 요청합니다.

- API 요청·응답 스키마와 endpoint 경로 변경
- `app/core/responses.py`, `app/core/errors.py`의 공통 응답·예외 계약 변경
- ChromaDB collection 이름, document ID, metadata 구조 변경
- 임베딩 모델 또는 Gemini 모델 변경
- 매칭 임계값·가중치·감점 규칙 변경
- 전사 및 회의 데이터 보존·로그 정책 변경

## 참조 구현 경로

| 파일 | 참조할 내용 |
| --- | --- |
| `app/domains/commit/router.py` | router 구성, Pydantic 응답 모델, 공통 응답 래핑 |
| `app/domains/commit/services/summarize.py` | Gemini 호출, 구조화 출력 파싱, 임베딩 생성 |
| `app/domains/meeting_analysis/services/extraction.py` | 프롬프트 빌더, JSON 출력 검증, 회의 분석 흐름 |
| `app/domains/transcribe/services/transcript_correction.py` | 프롬프트 상수, JSON 모드, 실패 시 fallback |
| `app/core/gemini.py` | 공통 timeout·retry·usage logging 정책 |
| `app/core/chroma.py` | ChromaDB 클라이언트와 컬렉션 관리 |
| `tests/test_gemini_retry.py` | 외부 LLM 호출 재시도 테스트 방식 |
| `tests/test_commit_matching_evaluation.py` | 매칭 품질 회귀 검증 방식 |

## AI에게 시킬 때 주의점

- 구현 요청에 이 문서와 수정 대상 도메인의 참조 구현 경로를 함께 제공합니다.
- LLM 기능을 수정할 때 입력, 기대 출력 스키마, 실패 처리, timeout·retry 정책을 먼저 설명하게 합니다.
- 프롬프트 변경 전후에 어떤 사례가 달라지는지와 회귀 위험을 요약하게 합니다.
- 임베딩·매칭 변경은 기준 데이터에 대한 전후 지표를 제시하게 합니다.
- API·ChromaDB 계약 또는 모델 변경이 필요하면 바로 수정하지 말고 Server 영향과 마이그레이션 방안을 먼저 제안하게 합니다.
- 구현이 끝나면 실행한 Ruff·pytest·평가 명령과 결과를 요약하게 합니다.
