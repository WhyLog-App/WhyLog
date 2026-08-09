# Server 개발 가이드

## 이 폴더의 문서

- `AGENTS.md` — Server 개발 규칙과 참조 구현 경로
- [`docs/review-checklist.md`](docs/review-checklist.md) — 머지 전 체크리스트. AI 리뷰의 판정 기준으로도 사용합니다.
- 새 문서를 추가하거나 경로를 변경하면 이 목차와 루트 `AGENTS.md`의 파트 인덱스를 함께 갱신합니다.

아래 경로 표기는 모두 `src/main/java/com/whylog/server/` 를 기준으로 합니다.

## 반드시 지킬 것

아래 규칙은 신규·수정 코드에 적용합니다. 변경하지 않은 기존 위반은 PR을 막지 않고 부채 이슈로 관리합니다.

### 계층

- 호출 방향은 `controller → service → repository` 한 방향만 허용합니다. 역방향 호출과 controller에서 repository 직접 호출을 금지합니다.
- 외부 시스템 연동 코드는 `global/external/` 아래에만 둡니다. 도메인 서비스는 그 클라이언트를 주입받아 사용합니다.
- 컨트롤러는 서비스 호출과 응답 래핑만 합니다. 분기·계산·엔티티 조작을 컨트롤러에 두지 않습니다.
- 서비스는 목적에 따라 세 종류로 나눕니다.
  - `XxxCommandService` — 상태를 변경하는 로직
  - `XxxQueryService` — 조회 전용 로직
  - `XxxUseCase` — **다른 도메인에서 재사용하는 단건 조회만** 담당합니다(`findTeamById` 등). 비즈니스 로직을 넣지 않습니다. → `domain/team/service/TeamUseCase.java`
- 서비스에 인터페이스와 `Impl`을 만들지 않습니다. 클래스로 직접 작성합니다.

### 트랜잭션

- `@Transactional`은 서비스 클래스에만 붙입니다. 컨트롤러와 리포지토리에는 붙이지 않습니다.
- `XxxCommandService`는 클래스 레벨에 `@Transactional`을 붙입니다.
- `XxxQueryService`는 클래스 레벨에 `@Transactional(readOnly = true)`를 붙입니다.
- 같은 클래스 내부 호출은 프록시를 거치지 않으므로, 호출 대상 메서드에 별도로 선언한 `@Transactional` 속성(전파·격리·`readOnly` 등)이 적용되지 않습니다. 호출자가 연 트랜잭션은 유지됩니다. 별도 트랜잭션 경계가 필요하면 다른 빈으로 분리합니다.
- **외부 호출(S3, FastAPI, LiveKit, GitHub API)을 트랜잭션 안에서 하지 않습니다.** 커밋 이후 실행이 필요하면 `TransactionSynchronizationManager`로 afterCommit에 등록합니다.

```java
// ❌ 트랜잭션 안에서 S3 업로드 — 업로드가 걸리는 시간만큼 DB 커넥션을 붙잡는다
@Transactional
public TeamCreateResponseDTO createTeam(...) {
    String imageKey = s3Client.uploadFile(image, ImageType.TEAM_IMAGE);
    teamRepository.save(Team.create(request, imageKey));
}

// ✅ 커밋 이후로 미룬다 → domain/team/service/TeamCommandService.java 의 removeTeam
teamRepository.delete(team);
scheduleAfterCommit(() -> s3Client.deleteFile(team.getImage()));
```

### 쿼리

- 반복문 안에서 리포지토리를 호출하지 않습니다.
- 연관 엔티티를 함께 조회할 때는 fetch join 또는 `@EntityGraph`를 사용합니다.
- 목록 조회 결과를 조합해야 하면 JPQL projection으로 평면 행을 받아 서비스에서 묶습니다. → `domain/team/repository/TeamRepository.java`의 `findDecisionRows`, `TeamQueryService.decisions`
- 새 조회 조건을 추가할 때는 인덱스가 필요한지 함께 검토하고, 판단 근거를 PR 본문에 남깁니다.

### 엔티티

- 모든 엔티티는 `global/entity/BaseEntity`를 상속합니다.
- 엔티티에 setter를 만들지 않습니다.
- 생성 방식은 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + private `@Builder` 생성자 + `static create(...)` 팩토리로 고정합니다. → `domain/team/entity/Team.java`

### 인증

- 인증된 사용자 식별자는 `@CurrentMember Long memberId`로 주입받습니다. `SecurityContextHolder`를 컨트롤러나 서비스에서 직접 조회하지 않습니다.

### 설정값 주입

- 같은 prefix를 공유하는 설정값이 3개 이상이면 `@ConfigurationProperties` 클래스 하나로 묶습니다.
- `@Value`는 prefix를 공유하지 않는 독립적인 값 1~2개에만 씁니다.
- `@Configuration`은 빈 생성과 조립만 담당합니다. 한 클래스에 `@Configuration`과 `@ConfigurationProperties`를 함께 붙이지 않습니다.
- 설정 클래스는 `global/config/` 아래에만 둡니다. 같은 외부 시스템의 설정값을 여러 클래스에 흩어 놓지 않습니다.

```java
// ❌ 같은 prefix를 @Value로 나눠 주입 — 설정 하나 바꾸려면 어느 클래스를 봐야 하는지 알 수 없다
@Value("${livekit.api-key}") private String apiKey;
@Value("${livekit.api-secret}") private String apiSecret;
@Value("${livekit.url}") private String url;

// ✅ 하나로 묶는다
@ConfigurationProperties(prefix = "livekit")
public record LiveKitProperties(String apiKey, String apiSecret, String url) {}
```

### 코드 작성 공통

- 빌드나 CI를 통과시키려고 실패하는 테스트를 삭제하거나 비활성화하지 않습니다.
- 타입·경고 검사를 우회하는 억제 패턴(근거 없는 `@SuppressWarnings`, 무분별한 캐스팅)을 쓰지 않습니다.
- 주석은 이름과 구조만으로 의도가 드러나지 않을 때만 답니다.

## 응답 · 예외 형식

- 컨트롤러는 `ResponseEntity`가 아니라 `ApiResponse<T>`를 반환합니다. 성공 응답은 `ApiResponse.onSuccess(result)`로 감쌉니다.
- 실패 응답을 직접 만들지 않습니다. 예외를 던지면 `ExceptionAdvice`가 형식을 맞춰 응답합니다.
- 예외는 `throw new ErrorHandler(XxxErrorCode.CODE)` 형태로 던집니다. 에러 하나마다 전용 예외 클래스를 새로 만들지 않습니다.
  - 기존 전용 예외 클래스(`TeamNotFoundException` 등)는 그대로 두되, 신규 작성은 하지 않습니다.
- 새 에러 코드는 도메인별 `XxxErrorCode` enum에 추가합니다. `BaseErrorCode`를 구현하고 `HttpStatus`, 코드 문자열, 한국어 메시지를 함께 정의합니다. → `domain/team/exception/TeamErrorCode.java`
  - enum 이름은 `XxxErrorCode`로 통일합니다. `XxxErrorStatus`는 신규로 만들지 않습니다.
- 컨트롤러 메서드에는 발생 가능한 에러를 `@ApiErrorCodeExamples` / `@ApiErrorCodeExample`로 명시하고, `@Operation`으로 요약과 설명을 답니다.
- DTO는 `XxxRequest` / `XxxResponse` 컨테이너 클래스 안의 static inner class로 만들고 이름을 `~DTO`로 끝냅니다. 각 필드에 `@Schema`를 답니다. → `domain/team/dto/TeamResponse.java`

## 테스트

- 현재 서버 테스트는 `@SpringBootTest`가 비활성화된 빈 JUnit `contextLoads` 테스트 1개뿐이며, Spring 컨텍스트를 검증하지 않습니다.
- **S2까지는 신규 테스트 작성을 요구하지 않습니다.** 테스트 부재를 이유로 PR을 막지 않습니다.
- S3부터 테스트 도입 범위를 회의에서 정하고 이 문서를 갱신합니다.

## 검증

가장 작은 검증부터 실행하고, 필요할 때만 범위를 넓힙니다. CI에 검증을 떠넘기지 않습니다.

Java 파일을 수정했다면 Spotless로 자동 정리한 뒤 포맷·기본 린트 검사를 먼저 통과시킵니다.

```bash
./gradlew spotlessApply
./gradlew spotlessCheck checkstyleMain checkstyleTest
```

```bash
./gradlew compileJava 2>&1 | tail -c 4000
```

전체 빌드가 필요할 때만 `./gradlew build`를 실행합니다. 문서만 변경한 경우에는 diff 확인으로 갈음합니다.

## 경계

아래는 담당자 확인 없이 수정하지 않습니다. AI에게 시킬 때도 바로 적용하지 말고 먼저 제안하게 합니다.

- `global/apiPayload/`, `global/entity/`, `global/external/` — 여러 도메인이 함께 깨집니다.
- `src/main/resources/application.yaml` — 운영 설정값이 들어 있습니다.
- 엔티티 필드·관계 변경 — 스키마에 영향을 줍니다.

## 참조 구현 경로

**`domain/team/` 전체를 참조 구현으로 삼습니다.** 파일 하나가 아니라 도메인 하나를 봅니다.

| 파일 | 참조할 내용 |
| --- | --- |
| `domain/team/controller/TeamController.java` | 컨트롤러 구조, Swagger 어노테이션, `ApiResponse` 반환 |
| `domain/team/service/TeamCommandService.java` | 트랜잭션 경계, afterCommit 처리, 예외 던지기 |
| `domain/team/service/TeamQueryService.java` | 조회 전용 서비스, 평면 행 조합 |
| `domain/team/service/TeamUseCase.java` | 도메인 간 공용 단건 조회 |
| `domain/team/entity/Team.java` | 엔티티 생성 규약 |
| `domain/team/exception/TeamErrorCode.java` | 에러 코드 정의 |
| `domain/team/repository/TeamRepository.java` | JPQL projection |
| `domain/team/dto/TeamResponse.java` | 응답 DTO 구조 |

## AI에게 시킬 때 주의점

- 구현을 시킬 때 위 참조 구현 경로와 이 문서를 함께 제공합니다.
- 조회 로직을 만들 때는 실행되는 쿼리 개수를 함께 설명하게 합니다.
- 구현이 끝나면 [`docs/review-checklist.md`](docs/review-checklist.md)로 스스로 점검한 결과를 요약하게 합니다.
