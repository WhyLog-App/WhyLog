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
- Deploy: AWS <!-- Docker · GitHub Actions 사용 시 추가 -->


## 서버 아키텍처
<!-- 아키텍처 다이어그램 이미지로 교체 (REST / WebSocket / LiveKit SFU 3축) -->
<img width="2616" height="1740" alt="Group 12" src="https://github.com/user-attachments/assets/d8ec7327-80fa-4c1f-b771-1cb4fe89f25d" />


- **REST API** — 인증, 팀·회의 관리, 회의 생성·종료, LiveKit 접속 토큰(rtc-token) 발급, GitHub 연동
- **WebSocket** — 참가자 동기화, 채팅, 실시간 자막, 입·퇴장 이벤트 브로드캐스트
- **LiveKit SFU (외부)** — 실시간 음성 스트림 중계. 백엔드는 미디어에 관여하지 않고 접속 토큰만 발급합니다.
- 실시간 자막은 클라이언트 STT 텍스트를 WebSocket으로 수신해 참가자에게 브로드캐스트합니다.


## API 문서
- Swagger UI: `/swagger-ui/index.html` (springdoc-openapi)


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
- {type}/{기능 요약}: 기능 개발용 브랜치입니다. (예: `feat/meeting`)

#### Commit Convention
커밋 타입을 접두로 사용합니다. (예: `feat: 회의 생성 기능 구현`)

| **Type** | **Description** |
| --- | --- |
| **feat** | 새로운 기능 추가 |
| **fix** | 버그 수정 |
| **docs** | 문서 수정 |
| **style** | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| **refactor** | 코드 리팩토링 |
| **test** | 테스트 코드, 리팩토링 테스트 코드 추가 |
| **chore** | 패키지 매니저 수정, 그 외 기타 수정 (예: .gitignore) |
| **design** | CSS 등 사용자 UI 디자인 변경 |
| **comment** | 필요한 주석 추가 및 변경 |
| **rename** | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우 |
| **remove** | 파일을 삭제하는 작업만 수행한 경우 |
| **init** | 프로젝트 초기 세팅 |
| **merge** | 브랜치 merge |
| **!BREAKING CHANGE** | 커다란 API 변경의 경우 |
| **!HOTFIX** | 급하게 치명적인 버그를 고쳐야 하는 경우 |

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
      <a href="https://github.com/팀원2">
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
