# Domain

WhyLog에서 사용하는 공통 용어와 비즈니스 규칙을 기록합니다.

## 제품 개요

WhyLog는 팀 회의를 녹음·전사하고 AI로 논의 주제와 결정 근거를 추출해, 그 결정이 실제로 어떤 Git 커밋으로 이어졌는지 매칭해준다. 목적은 "코드가 왜 이렇게 바뀌었는지"를 나중에도 추적할 수 있게 하는 것이다.

흐름: 회의 진행 → AI가 주제/근거/결정 추출 → 결정사항 저장(신뢰도 점수 포함) → Git 커밋 매칭.

## 용어

| 용어 | 뜻 |
| --- | --- |
| 회의 (Meeting) | 팀이 진행한 녹음 회의 단위 |
| 발화 (Dialogue) | 회의 중 한 명의 한 마디 발언 |
| 결정 (Decision) | 회의 하나에서 도출된 결정 기록 (Meeting과 1:1) |
| 적용사항 (Application) | 결정에서 파생된, 실제로 적용하기로 한 구체적인 항목 |
| 결정근거 (DecisionBase) | 결정을 뒷받침하는 근거 텍스트 |
| 결정 타임라인 (DecisionTimeline) | 결정이 도출되기까지의 단계별 서술. 이슈제기 → 대안논의 → 적용합의 3단계로 고정 |
| 신뢰도 (reliabilityScore / confidence) | 결정 또는 커밋 매칭 결과의 신뢰 정도를 나타내는 점수. Decision 자체의 신뢰도와 커밋 매칭 신뢰도는 서로 다른 값이니 혼동하지 않는다 |
| 커밋 매칭 (CommitConnection) | 적용사항(Application)과 실제 Git 커밋을 연결하는 관계 |
| 프로젝트 | 제품 문구에서 `Team`을 가리키는 이름 |
| 활성 프로젝트 | `TeamMember.active = true`인 프로젝트. 프로필 프로젝트 목록과 집계 대상은 활성 프로젝트만 포함한다 |
| 계정 상태 (accountStatus) | `UNVERIFIED`, `ACTIVE`, `WITHDRAWAL_PENDING`, `TOMBSTONED` 중 하나인 회원 생명주기 상태 |
| 본인 프로필 | 로그인 사용자가 자기 계정 정보와 계정 관리 기능을 보는 비공개 프로필 |
| 공개 멤버 프로필 | 로그인 사용자가 다른 멤버의 허용된 기본 정보와 집계만 보는 제한 공개 프로필 |
| 저장 커밋 수 | GitHub 전체 커밋 수가 아니라 Server DB에 동기화되어 저장된 커밋 수 |

## 핵심 엔티티와 관계

- Member 1—N TeamMember / Member는 `accountStatus`로 계정 생명주기를 가진다
- Team 1—N Meeting, Repository / Team N—N Member (TeamMember 조인, role: OWNER·MEMBER, active)
- Meeting 1—N Dialogue, MeetingMember(role: OWNER·GENERAL) / Meeting 1—1 MeetingAnalysis, Decision
- Decision 1—N Application, DecisionBase, DecisionTimeline, DecisionCommits
- Application N—N DecisionBase / DecisionTimeline / DecisionCommits (join 엔티티가 reason·confidence를 보유)
- Application N—N Commit (CommitConnection을 통해)
- Repository 1—N Commit / Commit 1—1 CommitAnalysis, 1—N ChangedFile

## 추천 매칭 vs 확정 연결 (중요)

적용사항과 커밋 사이에는 **성격이 다른 두 관계**가 동시에 존재한다. 둘을 혼동하면 안 된다.

| | AI 추천 매칭 | 사용자 확정 연결 |
| --- | --- | --- |
| 엔티티 | `DecisionCommits` + `ApplicationCommits` (N—N, reason·confidence 보유) | `CommitConnection` (N—N) |
| 만들어지는 시점 | `DecisionCommitMatchService.matchApplicationCommits` 호출 시 FastAPI(AI) 응답을 저장 | `ApplicationCommandService.connectCommit`으로 사용자가 직접 확정 |
| 재계산 시 동작 | 매번 **기존 추천을 전부 지우고(delete) 새 스냅샷으로 교체**한다 (append 아님) | 사용자가 명시적으로 해제(`disconnectCommit`)하기 전까지 유지 |
| 커밋 1개당 개수 제한 | 없음 — 같은 커밋이 여러 적용사항의 추천 후보가 될 수 있다 | **커밋 1개는 전체 적용사항을 통틀어 동시에 하나의 확정 연결만 가질 수 있다.** `connectCommit`은 요청에 포함된 커밋 중 하나라도 이미 다른 곳에 연결돼 있으면 요청 전체를 실패시킨다 |
| 추천 후보 필터링 | 이미 확정 연결(`CommitConnection`)된 커밋은 추천 목록에서 제외하고 저장한다 | — |

## 비즈니스 규칙

### 회의 (Meeting)

- 회의 상태는 저장하지 않고 계산한다: `endDateTime`이 없으면 진행중(ONGOING), 있으면 종료(COMPLETED)로 판단한다 (`Meeting.getStatus()`).
- 회의 생성자는 자동으로 해당 회의의 `MeetingMember`로 등록되며 역할은 OWNER다. 이후 참여자는 GENERAL로 추정된다.
- 회의 종료(`endMeeting`)는 아무 참여자(`MeetingMember`)나 호출할 수 있다. 소유자 권한을 요구하지 않는다 — `endDateTime`이 없는(진행중) 회의여야 하고, 참여자가 아니면 거부한다.
- 회의 **삭제**는 반드시 해당 회의의 OWNER만 할 수 있다. 종료보다 삭제가 더 강한 권한을 요구한다.
- 실시간 참여자가 모두 빠지면(`meetingSocketRoomService`의 참여자 목록이 비면) 서버가 자동으로 회의를 종료 처리한다(`autoEndMeetingIfEmpty`). 이때는 참여자에게 종료를 브로드캐스트하지 않는다(정상 종료와 구분).
- 회의 종료 시 `isNormallyEnded = true`로 세팅한다. 진행 시간(`getDuration`)은 `endDateTime`이 있는, 즉 종료된 회의에서만 계산 가능하다(진행중이면 null).
- 회의가 끝나면 커밋 이후 비동기로 오디오 분석(`MeetingAnalysisService.analyzeMeetingAudio`)이 트리거된다. 분석 실패는 로그만 남기고 회의 종료 자체를 실패시키지 않는다.
- 회의 삭제 시 녹음 중이면(`audioEgressId` 존재) 먼저 LiveKit egress를 중지하고, 실패해도 로그만 남기고 삭제를 계속 진행한다(녹음 중지 실패가 데이터 정리를 막지 않는다).

### 팀 (Team)

- 팀 이름은 팀 전체에서 유일해야 하고, 1~50자여야 한다.
- 팀 생성자는 자동으로 OWNER 역할의 `TeamMember`가 된다.
- 이미 활성(active=true) 멤버인 사람을 다시 초대할 수 없다.
- 팀 삭제는 반드시 해당 팀의 OWNER만 할 수 있다.
- 팀을 삭제하면 그 팀의 회의들도 함께 정리되고(`meetingCleanupService`), 삭제 커밋 이후 진행중이던 실시간 회의 방을 닫는다.

### 멤버 프로필과 계정 생명주기 (Member)

- 계정 상태는 `UNVERIFIED`(이메일 미인증), `ACTIVE`(정상 사용), `WITHDRAWAL_PENDING`(30일 탈퇴 유예), `TOMBSTONED`(PII·인증정보 제거 완료)만 사용한다.
- 기존 회원은 migration에서 `ACTIVE`, `email_verified_at = createdAt`, `purge_at = null`로 backfill한다.
- `UNVERIFIED` 회원은 일반 JWT를 받을 수 없고 서비스 화면에 접근할 수 없다. 미인증 보존 기한이 지나면 cleanup이 이메일 점유를 해제한다.
- `WITHDRAWAL_PENDING` 회원은 일반 서비스에 접근할 수 없고 복구 challenge만 사용할 수 있다. 30일 유예 안에는 `ACTIVE`로 복구할 수 있으며, 유예 중 타인 프로필과 허용된 과거 이력은 기존 표시를 유지한다. 유예 중 같은 이메일의 신규 가입은 복구로 안내한다.
- `TOMBSTONED` 회원의 직접 프로필은 `404`다. FK와 공유 이력은 유지하고, 허용된 프로젝트 과거 화면에는 `탈퇴한 사용자`로 표시한다.
- 본인 프로필 DTO에만 이메일, 계정 상태·기능, 최근 완료 회의·결정, GitHub 재연결·오류 정보를 포함한다. 공개 멤버 프로필 DTO에는 이 필드를 포함하지 않는다.
- 프로필 프로젝트 목록은 `TeamMember.active = true`인 프로젝트만 포함한다. 프로젝트에서 나가면 양쪽 프로필 목록에서 즉시 빠지고, 프로젝트 내부 과거 기록에는 `나간 사용자`로 남는다.
- 탈퇴 요청은 `ACTIVE` 회원만 가능하다. 하나라도 유일 OWNER인 활성 프로젝트가 있으면 탈퇴를 거부한다.
- 탈퇴 완료 cleanup은 이메일, 비밀번호, 이름, 프로필 이미지 참조, GitHub 토큰 등 PII·인증정보를 제거하되 회의·발화·결정 이력은 삭제하지 않는다.
- signup/login/email-change 이메일은 `trim` + `Locale.ROOT` lowercase로 정규화한 값을 `member.email`에 저장한다. 정규화 migration은 중복 계정을 병합하지 않고 중복 발견 시 중단한다.
- 가입 인증, 이메일 변경, 복구 토큰은 해시된 일회용 토큰이며 기한을 가진다. 이메일 변경은 현재 비밀번호 재확인과 새 이메일 인증이 모두 끝나야 적용된다.
- 비밀번호나 이메일을 변경하면 기존 refresh token을 폐기한다.
- 이메일 발송과 S3 이미지 삭제는 outbox로 기록한 뒤 커밋 이후 처리한다. 실패는 성공으로 간주하지 않고 재시도 가능해야 한다.

### 마이페이지·공개 프로필 통계

- 통계는 활성 프로젝트별로만 제공한다.
- 회의 수와 누적 회의 시간은 완료 회의(`endDateTime != null`)만 포함한다. 진행 중 회의는 제외한다.
- 누적 회의 시간은 `startDateTime`과 `endDateTime`의 차이를 초 단위로 모두 합산한 뒤 화면에서 포맷한다. 회의별 분 단위 절삭·반올림 후 합산하지 않는다.
- 개인 회의 통계는 대상 멤버가 `MeetingMember`로 참여한 완료 회의만 센다.
- 프로젝트 회의 통계는 프로젝트의 완료 회의 전체를 센다.
- 저장 커밋 수는 프로젝트의 모든 Repository에 저장된 Commit 수 합계다. 개인 커밋 수나 개인 커밋 매핑은 제공하지 않는다.
- 본인 프로필에만 최신 완료 회의와 최신 결정을 기본 5개씩 제공한다. 공개 멤버 프로필에는 최근 기록과 커밋 피드를 제공하지 않는다.
- 공개 멤버 프로필의 저장 커밋 최신성은 집계 내부 메타데이터 `SYNCED`, `STALE`, `UNAVAILABLE`과 기준시각만 노출한다. provider 오류, 토큰 상태, 저장소 URL, 내부 job ID는 노출하지 않는다.

### GitHub 연동 (Member)

- GitHub 액세스 토큰은 AES로 암호화해 저장한다(`AESCryptoConverter`).
- GitHub API가 401을 반환하면 저장된 토큰을 즉시 폐기한다(`clearGithubToken`) — 만료된 토큰을 계속 재사용하지 않는다.
- 프로필용 GitHub refresh는 `ACTIVE` 본인만 request body 없이 요청할 수 있고 DB 트랜잭션 밖에서 실행한다. 본인의 GitHub 토큰으로 접근 가능한 모든 활성 프로젝트·저장소를 enqueue하며, member/repository dedupe, repository cooldown, member rate limit을 적용한다. 타인 프로필 조회는 외부 동기화를 시작하지 않는다.

### 커밋 · 레포지토리 (Git)

- 커밋은 `(repository_id, hash)` 조합으로 유일해야 한다.
- 커밋 분석은 `queued → summarizing → summary_ready → embedding → embedding_ready → failed` 단계를 거치며, `embedding_ready`가 되기 전에는 매칭 API(`/api/commit/match`)를 호출할 수 없다.

### 결정 타임라인 (DecisionTimeline)

- 단계는 이슈제기(ISSUE) → 대안논의(DISCUSSION) → 적용합의(AGREEMENT) 3단계로 고정이며 순서를 바꾸지 않는다.

### 커밋-적용사항 매칭 신뢰도 계산 (AI, `ai/app/domains/meeting_analysis/services/matching_scoring.py`)

기본 점수는 `semantic(0~50) + keyword(0~30) + context(0~20)`이며, 여기에 커밋 타입 가산(`type_bonus`, 최대 +3)을 더하고 감점(`penalty`)을 뺀 뒤 0~100으로 clamp한다.

- **semantic (의미 유사성, 0~50)**: 임베딩 코사인 거리를 `(1 - distance) * 50`으로 환산. 방향이 반대(아래 참조)면 무조건 0점.
- **keyword (기술 키워드 겹침, 0~30)**: 겹치는 키워드 0개=0점, 1개=15점, 2개=25점, 3개 이상=30점.
- **context (모듈/경로 맥락, 0~20)**: 모듈 토큰이 2개 이상 직접 겹치면 20점, 1개 겹치면 10점, 직접 겹치지 않아도 prefix/substring으로 걸리면 10점, 아니면 0점.
- **type_bonus**: 커밋 메시지의 conventional-commit 타입(`feat`/`fix`/`docs`/…)과 적용사항에서 추론한 예상 타입이 맞으면 +3점.
- **penalty**: 커밋 메시지가 너무 추상적이면(`is_abstract_commit_message`, 의미 있는 토큰이 사실상 없음) +10점, 적용사항 자체가 모호하면(`is_ambiguous_application`, 토큰 3개 미만이거나 키워드·모듈이 전혀 없음) +10점 — **최대 20점까지 감점**될 수 있다.
- **반대 방향(opposite direction) 처리**: 적용사항과 커밋 각각에 `positive`/`negative` 방향 라벨이 있고 서로 반대(예: "추가하기로 함" vs "제거하는 커밋")면, semantic 점수를 0으로 만들고 해당 조합은 자동 연결 대상에서 제외된다.
- **목적 불일치(goal mismatch) 처리**: 키워드 점수가 15점 이상인데 semantic 점수가 10점 이하면("겹치는 단어는 있지만 의미가 다른" 경우) 전체 점수를 0점·UNAPPLIED로 강제하고 자동 연결하지 않는다 — 우연히 단어만 겹친 오탐을 막기 위한 규칙.
- **매칭 상태(MatchStatus)**: 총점 70점 이상 APPLIED, 50~69점 PARTIAL, 그 미만 UNAPPLIED.
- **동일 커밋에 대한 상충 매칭 정리**: 한 커밋이 서로 반대 방향인 적용사항 여러 개와 동시에 높은 점수로 매칭되면, 점수가 더 높은 쪽만 남기고 반대 방향 매칭은 제거한다(`_resolve_conflicting_matches`).
- **서버 쪽 최종 노출 기준**: 위 계산과 별개로, 서버(`DecisionCommitMatchService`)는 confidence 70점 이상인 추천만 저장·노출한다(`MIN_RECOMMENDATION_CONFIDENCE`). AI 쪽 PARTIAL(50~69점) 매칭은 계산은 되지만 최종 추천에는 포함되지 않는다.
- **결정 신뢰도(Decision.reliabilityScore)**: 그 결정에 저장된 모든 추천 매칭 confidence의 평균을 반올림한 값이다. 추천이 하나도 저장되지 않으면 null로 초기화된다.
