# 🎙️ WhyLog Back-End

## 프로젝트 소개
회의에서 커밋까지, 의사결정 맥락을 추적하는 실시간 음성 회의 협업 플랫폼
회의에서 나온 논의·결정을 GitHub 커밋/PR과 연결해, "왜 이렇게 개발했는가"의 맥락을 남깁니다.

<div align="center">
<!-- 대표 이미지(서비스 소개 배너)로 교체 -->
<img width="4500" height="3000" alt="WhyLog" src="https://github.com/user-attachments/assets/5dec6685-8d5b-439d-be3e-d7974928a228" />
</div>

#### 주요 기능
- **실시간 다자간 음성 회의**: WebRTC(LiveKit SFU) 기반으로 여러 참가자가 동시에 음성으로 회의를 진행합니다.
- **실시간 자막 & 채팅**: 발화 내용을 발화자별로 구분해 실시간 자막으로 표시하고, 채팅 메시지를 브로드캐스트합니다.
- **회의 기록 및 분석**: 발화 기록을 저장하고 텍스트 기반으로 회의 내용을 분석합니다.
- **GitHub 연동 — 의사결정 추적**: 회의에서 나온 논의·결정을 GitHub 커밋/PR과 연결해 개발 맥락을 추적합니다.
- **팀 · 회의 관리**: 팀을 구성하고, 팀 단위로 회의를 생성·진행·종료합니다.
- **파일 업로드**: AWS S3 기반 파일·이미지 업로드를 지원합니다.
- **인증**: JWT 기반 로그인 및 접근 제어를 제공합니다.


## 기술 스택
- Language: Java 17
- Framework: Spring Boot, Spring Security, Spring Data JPA, Spring AOP
- Real-time: WebSocket, WebRTC (LiveKit SFU)
- Auth: JWT (jjwt)
- Database: MySQL, Redis
- Storage: AWS S3
- Integration: GitHub API (org.kohsuke)
- API Docs: Swagger (springdoc-openapi)
- Logging: Log4j2
- Build Tool: Gradle
- Deploy: AWS
- Local Dev: Docker Compose (MySQL, Redis)


## 서버 아키텍처
<!-- 아키텍처 다이어그램 이미지로 교체 (REST / WebSocket / LiveKit SFU 3축) -->
<img width="2616" height="1740" alt="Group 12" src="https://github.com/user-attachments/assets/d8ec7327-80fa-4c1f-b771-1cb4fe89f25d" />


- **REST API** — 인증, 팀·회의 관리, 회의 생성·종료, LiveKit 접속 토큰(rtc-token) 발급, GitHub 연동
- **WebSocket** — 참가자 동기화, 채팅, 실시간 자막, 입·퇴장 이벤트 브로드캐스트
- **LiveKit SFU (외부)** — 실시간 음성 스트림 중계. 백엔드는 미디어에 관여하지 않고 접속 토큰만 발급합니다.
- 실시간 자막은 클라이언트 STT 텍스트를 WebSocket으로 수신해 참가자에게 브로드캐스트합니다.


## API 문서
- Swagger UI: `/swagger-ui/index.html` (springdoc-openapi)


## 실행 방법

두 방법 모두 서버 앱 자체는 로컬에서 `./gradlew bootRun`으로 실행합니다. 차이는 MySQL/Redis를 어떻게 붙이느냐입니다.

```bash
cp .env.example .env
# .env를 열어 값 채우기 (JWT_SECRET, GMAIL_SMTP_*, EMAIL_VERIFICATION_CODE_SECRET 등)
```

### 이메일 인증 발송 설정

가입 인증 메일은 Gmail SMTP로 발송하므로 실제 서버 실행 전에 아래 값을 채웁니다.

- `GMAIL_SMTP_USERNAME`: 실제 발신에 사용할 Gmail 주소
- `GMAIL_SMTP_APP_PASSWORD`: Google 계정 일반 비밀번호가 아니라 Gmail SMTP용 Google 앱 비밀번호입니다.
- `EMAIL_VERIFICATION_CODE_SECRET`: 인증 코드 HMAC에 사용할 32바이트 이상의 임의 문자열

Google 계정의 2단계 인증을 켠 뒤 앱 비밀번호를 발급해 `.env` 또는 배포 환경변수에만 저장합니다. 실제 값은 Git에 커밋하지 않습니다.

- Google 앱 비밀번호 안내: <https://support.google.com/accounts/answer/185833>

### 1) Docker Compose로 실행

로컬에 MySQL/Redis를 직접 설치하지 않고 컨테이너로 띄웁니다.

```bash
./scripts/dev.sh
```

`dev.sh`는 먼저 `.env`의 필수 값이 채워졌는지 검사하고(`scripts/check-env.sh`), 통과하면 `docker compose up -d`로 MySQL(3306)·Redis(6379)를 기동한 뒤 `.env` 값을 환경변수로 로드해 `./gradlew bootRun`을 실행합니다. 필수 값이 비어 있으면 어떤 변수인지 출력하고 종료합니다. 각 단계를 직접 실행하려면:

```bash
docker compose up -d --wait   # MySQL(3306), Redis(6379) 기동 후 healthy가 될 때까지 대기
set -a; source .env; set +a   # .env 값을 환경변수로 로드 (zsh/bash)
./gradlew bootRun
```

- 종료: `docker compose down` (데이터를 지우려면 `docker compose down -v`)
- `.env.example`의 `DEV_DB_URL`, `DEV_REDIS_HOST` 등은 이 compose 설정에 맞춰져 있어 그대로 쓰면 됩니다.

### 2) Docker 없이 실행

로컬에 MySQL 8, Redis를 직접 설치해 기동하거나(예: `brew install mysql redis`), 팀이 공유하는 개발용 DB/Redis에 접속합니다.

```bash
# 예: 로컬 설치 시
brew install mysql redis ffmpeg   # ffprobe는 오디오 길이 분석에 필요
brew services start mysql
brew services start redis
```

- `.env`의 `DEV_DB_URL`, `DEV_DB_USERNAME`, `DEV_DB_PASSWORD`, `DEV_REDIS_HOST`, `DEV_REDIS_PORT`, `DEV_REDIS_PASSWORD`를 실제 접속 정보로 맞춥니다.
- `AUDIO_FFPROBE_COMMAND`를 로컬 ffprobe 경로로 맞춥니다 (macOS Homebrew 기준 기본값은 `/opt/homebrew/bin/ffprobe`).

```bash
./scripts/check-env.sh        # 필수 값이 채워졌는지 검사
set -a; source .env; set +a
./gradlew bootRun
```

IntelliJ를 쓴다면 터미널 대신 아래처럼 실행해도 됩니다.

1. `Run` → `Edit Configurations` → `ServerApplication`의 `Environment variables`에서 값을 채웁니다.
   - 직접 하나씩 입력하거나, 옆의 폴더 아이콘(`Paste`/`파일 아이콘`)으로 `.env` 파일을 그대로 불러와도 됩니다.
2. `ServerApplication.java`를 열고 상단(또는 좌측) 실행 버튼을 누르면 됩니다.


## 프로젝트 구조
#### 도메인형
- 각 도메인 패키지는 엔티티, DTO, 컨트롤러, 서비스, 리포지토리 등 하위 패키지를 포함
- Base package: `com.whylog.server`

```
src/
└── main/
    └── java/com/whylog/server
        ├── ServerApplication.java
        ├── global/          # config, apiPayload, common (공통 응답·예외·설정)
        ├── auth/            # 인증 · JWT
        ├── member/          # 회원
        ├── team/            # 팀
        ├── meeting/         # 회의 생성 · 상태 · 참가자
        ├── chat/            # 채팅 · 자막 메시지
        └── ...              # (실제 도메인 폴더로 교정)
```
> 위 트리는 예시입니다. 실제 도메인 폴더 이름으로 교정하세요.

#### Branch Strategy
- main: 배포 가능한 최종 코드만 관리합니다.
- develop: 완성된 기능을 지속적으로 병합하는 브랜치입니다.
- 작업 브랜치 이름은 루트 `AGENTS.md`의 `<type>/<short-description>` 규칙을 따릅니다. (예: `feat/meeting-summary`)

#### Commit Convention
루트 `AGENTS.md`의 커밋 메시지 규칙을 따릅니다. 서버만 변경한 예시는 `feat(server): 회의 생성 기능 구현`이며, 여러 파트를 함께 변경하면 scope를 생략합니다.

#### Pull Request (PR)
- 본인을 Assignee로 지정하고, 팀원 1명 이상의 승인을 받은 뒤 develop 브랜치로 머지합니다.
- 관련 이슈가 있다면 연결합니다.


## 👥 Members
<div align="center">

<table>
  <tr>
    <!-- 팀원 수만큼 td 추가/삭제 -->
    <td align="center">
      <a href="https://github.com/ggamnunq">
        <img width="170" src="https://avatars.githubusercontent.com/u/93406666?v=4" alt="김준용" />
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Yujin1219">
        <img width="170" src="https://avatars.githubusercontent.com/u/127809173?v=4" alt="팀원2" />
      </a>
    </td>
  </tr>
  <tr>
    <td align="center"><b>김준용</b></td>
    <td align="center"><b>유진</b></td>
  </tr>
  <tr>
    <td align="center">유저·실시간회의·팀</td>
    <td align="center">깃·결정사항·적용사항</td>
  </tr>
</table>
</div>
