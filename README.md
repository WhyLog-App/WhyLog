# 💙 WhyLog Backend
## 🚀 Git Flow

- `main`
    - 프로젝트 최종 merge
    - 기본 프로젝트 세팅, 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `develop`
    - 완성한 기능들을 계속해서 merge
    - 배포 가능한 브랜치, 항상 배포 가능한 상태를 유지
- `{type}/{기능 요약}`
    - 개발 브랜치

> 브랜치 생성 → 생성한 브랜치에서 작업 후 끝나면 develop 브랜치로 PR 남기기
> 
> 
> 모든 작업 시작 전 생성한 브랜치에서 develop 브랜치 pull을 받은 후 작업
> 

## 💡 PR Rules

- Assignee에는 본인을 지정해 주세요.
- Reviewers에는 본인을 제외한 백엔드 팀원 후, 디스코드로 공유해 주세요.
- 이후, 팀원(1명 이상)이 PR을 확인하고 승인해서 머지해 주세요.
(해당 브랜치는 머지 후 자동 삭제되며, 복구도 가능합니다.)

## 💻 Commit Message Convention

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

> Type: commit title
> 
> 
> ex. `feat: 로그인 기능 추가`
>
