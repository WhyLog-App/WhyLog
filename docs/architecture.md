# Architecture

WhyLog의 AI, Server, Web 간 연결 구조와 서비스 흐름을 기록합니다.

## 전체 구성

```
Web (React, axios + WebSocket)
   │ REST (JWT Bearer)         │ WebSocket (실시간 회의 signaling)
   ▼                            ▼
Server (Spring Boot)  ──REST──▶  ai (FastAPI)
   │        │                        │
   │        │                        ├─ Gemini (텍스트 생성·임베딩)
   │        │                        ├─ Deepgram (음성 전사)
   │        │                        └─ ChromaDB (커밋·적용사항 임베딩)
   │        └──REST──▶ LiveKit (실시간 오디오/비디오 SFU + 녹음 egress)
   └──REST──▶ GitHub (커밋 이력 조회, kohsuke GitHub API)
   └──S3
```

Web과 LiveKit은 예외적으로 직접 연결된다(RTC). 그 외 모든 통신은 Server를 거친다 — ai·GitHub·S3·LiveKit 토큰 발급은 Web이 직접 호출하지 않는다.

## 파트별 역할

| 파트 | 책임 | 책임이 아닌 것 |
| --- | --- | --- |
| **Web** | 화면, 브라우저 Web Speech API로 실시간 자막 생성, LiveKit RTC 클라이언트 연결, Server REST/WebSocket 호출 | 원문 회의 분석, 커밋 매칭 점수 계산, 파일 저장 — 전부 Server·ai가 한다 |
| **Server** | 도메인 상태(Team/Meeting/Decision/Application/Commit)의 정본 저장, 외부 시스템(ai·LiveKit·GitHub·S3) 오케스트레이션, 인증 | 전사·요약·임베딩·매칭 점수 계산 자체는 하지 않는다 — 전부 ai에 위임하고 결과만 저장 |
| **ai** | 음성 전사(Deepgram), 회의 분석·적용사항 추출(Gemini), 커밋 요약·임베딩(Gemini), 임베딩 유사도 기반 매칭 점수 계산(ChromaDB) | 도메인 데이터의 정본을 갖지 않는다 — 매 요청마다 필요한 컨텍스트를 Server가 실어 보낸다. 인증/인가도 하지 않는다 |

## 인증

- Server가 로그인 시 JWT access/refresh 토큰을 발급한다.
- Web은 access 토큰을 메모리에만 보관하고, 매 요청에 `Authorization: Bearer`로 주입한다. 401을 받으면 refresh로 재발급을 시도한다.
- ai는 별도 인증이 없다 — Server만 호출한다는 전제로 신뢰 경계 안에 둔다.

## 서비스 흐름 1 — 실시간 회의 (Web ↔ LiveKit, Web ↔ Server)

회의 중 오디오/비디오와 "실시간 텍스트"는 서로 다른 두 경로로 흐른다.

- **오디오/비디오**: Web은 Server가 발급한 토큰으로 LiveKit에 직접 연결해 RTC를 주고받는다. LiveKit이 room 전체 오디오를 녹음(egress)해 S3에 저장하고, 이 원본 오디오가 회의 종료 후 "흐름 2"의 공식 전사 입력이 된다.
- **실시간 텍스트**: Web이 브라우저 Web Speech API로 발화를 즉석에서 전사하고, 그 결과를 Server가 여는 별도의 WebSocket으로 보낸다. Server는 이를 참가자에게 브로드캐스트하는 동시에 회의별로 버퍼링해둔다 — 화면 자막용이자, 회의 종료 후 공식 분석에 참고 컨텍스트로 함께 전달된다.
- 회의 종료(참여자가 직접 종료하거나, 실시간 참여자가 전부 빠져서 자동 종료)는 항상 같은 처리로 수렴한다: 녹음 중지 → 방 정리 → "흐름 2" 비동기 시작.

## 서비스 흐름 2 — 회의 종료 후 AI 분석 (Server ↔ ai)

1. Server가 녹음된 오디오와 실시간 텍스트 버퍼를 ai에 넘겨 전사+분석 실행(run)을 비동기로 시작시킨다.
2. **ai**가 Deepgram으로 오디오를 전사하고, Gemini로 논의 주제·결정 근거·적용사항·타임라인을 추출한다. 진행 단계는 전사중 → 전사완료 → 요약완료 → 적용사항추출완료로 나뉘며, Server는 완료될 때까지 상태를 조회(폴링)한다.
3. **Server**가 결과를 도메인 엔티티로 저장한다: 발화 목록, 회의 요약, 결정(Decision, 회의당 1개), 적용사항과 그 근거·타임라인. **적용사항 계열은 매번 전부 삭제 후 재생성한다 — 재분석은 append가 아니라 스냅샷 교체다.**
4. 저장이 끝나면 Server가 그 적용사항들을 다시 **ai**에 보내 임베딩을 만들게 하고(ChromaDB 저장), 이어서 커밋 자동 매칭을 요청한다. 이 두 단계는 실패해도 전체 흐름을 되돌리지 않는 best-effort다 — 분석 결과 저장 자체는 이미 끝났기 때문이다.
5. 매칭 점수 계산 로직은 **ai**에 있고(`docs/domain.md` 참조), Server는 결과를 신뢰도 임계값으로 한 번 더 걸러 저장할 뿐 점수 자체를 계산하지 않는다.

## 서비스 흐름 3 — GitHub 커밋 수집 및 분석 (Server ↔ GitHub ↔ ai)

GitHub는 webhook이 아니라 **Server가 사용자 요청 시 끌어오는(pull) 방식**이다.

1. 사용자가 GitHub 토큰을 등록하면 Server가 유효성을 검증해 암호화 저장한다. 토큰이 401을 반환하면(만료) Server가 즉시 폐기한다.
2. 레포지토리 동기화 시 Server가 GitHub REST API로 마지막 동기화 이후의 커밋만 가져온다. 이미 저장된 해시는 건너뛰고, 머지 커밋(부모 2개 이상)은 저장하지 않는다.
3. 새로 저장된 커밋(과 이전에 임베딩이 안 끝난 커밋)마다 Server가 변경 파일 목록과 함께 **ai**에 분석을 요청한다. ai가 Gemini로 요약·임베딩을 만들어 ChromaDB에 저장한다.
4. 이 파이프라인이 끝나야(임베딩 완료) "흐름 2"의 커밋 자동 매칭이 그 레포지토리의 커밋을 후보로 찾을 수 있다 — **레포 동기화가 회의 분석보다 먼저 되어 있어야 매칭이 의미가 있다.**

## 서비스 흐름 4 — 사용자의 수동 커밋 연결 (Web ↔ Server)

AI 추천 매칭과 별개로, Web에서 사용자가 적용사항에 커밋을 직접 확정 연결하거나 해제할 수 있다. 이 확정 연결은 재분석으로도 지워지지 않으며, 이후 추천 매칭 계산 시 그 커밋을 후보에서 제외한다. 두 관계의 차이는 `docs/domain.md`의 "추천 매칭 vs 확정 연결" 참조.

## 서비스 흐름 5 — 마이페이지·멤버 프로필 (Web ↔ Server)

- 프로필 API는 본인용과 공개용을 분리한다. 본인 API만 이메일, 계정 기능, 최근 완료 회의·결정, GitHub 재연결/오류 상태를 담고, 공개 API는 이름·이미지·활성 프로젝트와 승인된 집계만 담는다.
- 계정 생명주기는 `UNVERIFIED → ACTIVE → WITHDRAWAL_PENDING → TOMBSTONED`로 확장한다. 기존 `Member` 행과 FK는 보존하고, 계정 탈퇴 완료 시 PII·인증정보만 제거한다.
- 프로필 프로젝트 목록은 `TeamMember.active = true`인 팀만 사용한다. 기존 팀 설정 기능과 프로젝트 내부 과거 기록은 이 계약으로 바꾸지 않는다.
- 통계는 완료 회의(`endDateTime != null`)만 대상으로 한다. 회의 시간은 회의별 분 반올림 없이 초 단위 차이를 합산하고, 저장 커밋 수는 프로젝트의 모든 Repository에 저장된 Commit 수 합계만 제공한다. 개인 커밋 통계는 만들지 않는다.
- `POST /api/members/me/profile/refresh`는 request body 없이 본인 프로필 갱신을 요청한다. `ACTIVE` 본인의 GitHub 토큰으로 접근 가능한 모든 활성 프로젝트·저장소를 enqueue하고, 타인 프로필 조회는 DB 스냅샷만 반환한다.

## 마이페이지·멤버 프로필 구현 경계

### API·스키마 경계

- API/DTO는 `SelfProfileResponseDTO`와 `MemberPublicProfileResponseDTO`처럼 self/public을 분리한다. public DTO에는 이메일, 계정 상태, 최근 기록, provider 오류, 저장소 URL, token/reconnect 정보를 직렬화하지 않는다.
- 스키마 변경은 additive로 시작한다: `member.account_status`, `email_verified_at`, `purge_at`, 이메일 인증 토큰, 이메일 변경 예약/토큰, email outbox, external cleanup outbox. 기존 회원은 backfill에서 `ACTIVE`, `email_verified_at = createdAt`, `purge_at = null`로 채운다.
- signup/login/email-change 이메일은 service에서 `trim` + `Locale.ROOT` lowercase로 정규화하고, 기존 `member.email` unique 컬럼에 정규화 값을 저장한다. Migration은 정규화 전 중복을 먼저 감사하고, 중복이 있으면 계정을 병합하지 않고 중단한다.
- 인증·이메일 변경·복구 토큰은 원문을 저장하지 않고 해시된 일회용 토큰으로 저장한다. 각 token table은 `token_hash` unique를 가진다. pending email canonical value는 pending 상태 동안 unique여야 한다.
- 필수 index는 `member(email)` unique, token member/status/expiry 조회 index, outbox `status/next_attempt_at` index, member `account_status/purge_at` cleanup index다. `TeamMember(member_id, active)`와 회의 완료/profile aggregate index는 query plan으로 검토한다.
- 팀 설정, 친구, 채팅, 차단, 팀/친구 공개 범위, 개인 커밋 매핑·통계는 이번 계약에 포함하지 않는다.

### 트랜잭션 경계

- 이메일 인증·변경 토큰과 email outbox는 같은 DB 트랜잭션에 기록하고, 실제 이메일 발송은 커밋 이후 worker가 처리한다.
- S3 프로필 이미지 삭제는 DB 트랜잭션에서 참조 제거와 cleanup outbox만 기록하고, 실제 객체 삭제는 커밋 이후 worker가 처리한다. 실패는 재시도 가능한 상태로 남긴다.
- GitHub refresh는 self-only 권한 확인과 DB 스냅샷 응답을 분리하고, GitHub IO를 DB 트랜잭션 밖에서 실행한다. enqueue는 member/repository 단위로 dedupe하고, repository 단위 cooldown과 member 단위 rate limit을 적용한다. 토큰·권한 실패는 기존 저장값을 지우지 않고 본인 응답에만 복구 정보를 노출한다.
- 탈퇴 요청은 유일 OWNER 검사, `WITHDRAWAL_PENDING` 전환, 세션·GitHub 토큰 폐기를 한 트랜잭션에서 처리한다. 30일 purge는 멱등 scheduler가 `TOMBSTONED` 전환과 PII 제거를 처리한다.

### rollout·migration 순서

1. 이메일 정규화 중복을 감사한 뒤 additive 컬럼·토큰·outbox 테이블을 추가하고 기존 회원을 `ACTIVE`, `email_verified_at = createdAt`, `purge_at = null`로 backfill한다. 중복이 있으면 migration을 중단한다.
2. self/public DTO와 접근 정책 경계를 먼저 도입해 기존 팀 설정 응답과 호환성을 확인한다. 새 profile GET/update Web 흐름이 live될 때까지 기존 `/api/members/profile-image`는 compatibility alias로 유지한다.
3. 이메일 인증·이메일 변경·비밀번호 변경·탈퇴/복구 생명주기를 순차 적용한다.
4. 완료 회의 초 단위 집계와 프로젝트 저장 커밋 합계 projection을 추가한다.
5. 본인 전용 GitHub refresh와 outbox worker를 연결하고 부분 실패 상태를 검증한다.
6. Web 라우트(`/mypage`, `/members/:memberId`, 인증/복구 확인 화면)를 연결한다.

### guardian review points

- Server guardian: public/self DTO, 계정 상태 전이, 이메일 점유 unique/index, outbox transaction, purge FK 보존을 승인한다.
- Web guardian: `/mypage`와 `/members/:memberId` 필드 노출 차이, 모바일 확장 상태, 기존 팀 설정 UI 호환성을 승인한다.
- 공용 계약 변경은 Server/Web 양쪽 가디언 확인 뒤 적용한다. 이메일 발송 업체나 운영 자격증명은 이 문서에서 정하지 않는다.

## 경계와 소유권

- **ai를 직접 호출하는 곳은 Server 안의 한 경계 뿐이다.** 도메인 서비스가 ai SDK나 HTTP 호출을 직접 들고 있지 않고, 전용 클라이언트를 주입받아 쓴다. LiveKit·S3·GitHub 연동도 같은 원칙으로 별도 경계에 모여 있다.
- **ai는 도메인 데이터의 정본을 갖지 않는다.** ChromaDB 임베딩은 매칭 계산용 파생 데이터일 뿐, 결정·적용사항·커밋 연결의 정본은 항상 Server DB다.
- ai 쪽에서 프롬프트·매칭 가중치·ChromaDB 스키마를 바꾸면 Server가 파싱하는 응답 형태도 함께 깨질 수 있어 담당자 확인이 필요하다.
- 외부 호출(S3, ai, LiveKit, GitHub)은 Server의 트랜잭션 안에서 하지 않는다. 커밋 이후 실행이 필요하면 afterCommit 시점으로 미룬다.
