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

## 핵심 엔티티와 관계

- Team 1—N Meeting, Repository / Team N—N Member (TeamMember 조인, role: OWNER·MEMBER)
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

### GitHub 연동 (Member)

- GitHub 액세스 토큰은 AES로 암호화해 저장한다(`AESCryptoConverter`).
- GitHub API가 401을 반환하면 저장된 토큰을 즉시 폐기한다(`clearGithubToken`) — 만료된 토큰을 계속 재사용하지 않는다.

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
