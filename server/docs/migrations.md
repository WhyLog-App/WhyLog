# DB 마이그레이션 · 목데이터

## Flyway

- 스키마는 `src/main/resources/db/migration/`의 Flyway 마이그레이션 파일로만 변경합니다. `spring.jpa.hibernate.ddl-auto`는 `validate`이므로 엔티티만 바꾸고 마이그레이션 파일을 추가하지 않으면 애플리케이션이 기동 시 검증에 실패합니다.
- 파일명은 `V{version}__{설명}.sql` 형식을 씁니다. `version`은 이전 마이그레이션보다 큰 정수(또는 `1.1`처럼 점으로 구분한 정수)를 순서대로 매기고, `설명`은 영문 소문자 snake_case로 씁니다. 예: `V2__add_meeting_summary_status.sql`.
- 버전을 한 번 매긴 파일은 수정하지 않습니다. 이미 실행된 마이그레이션 파일을 고치면 체크섬이 달라져 다음 기동이 실패합니다. 수정이 필요하면 새 버전 파일을 추가합니다.
- Flyway는 DB의 `flyway_schema_history` 테이블에 마지막으로 적용한 버전을 기록해두고, 기동할 때마다 `db/migration`에서 그보다 버전이 큰 파일만 순서대로 찾아 실행합니다. 새 마이그레이션 파일을 추가하고 서버를 재기동하면 자동으로 반영됩니다.
- `spring.flyway.baseline-on-migrate: true`가 켜져 있어, `flyway_schema_history`가 없는 기존 DB(V1 도입 이전에 `ddl-auto: update`로 만들어진 DB)에서도 V1을 기준선으로 잡고 그 이후 마이그레이션만 적용합니다.

## mock.sql

- `src/main/resources/db/mock/mock.sql`은 화면 테스트용 목데이터입니다. Flyway 마이그레이션이 아니므로 서버 기동 시 자동 실행되지 않습니다.
- 로컬DB에서 필요할 때 직접 실행합니다. ( DataGrip, Mysql Workbench 등 이용 )

- 실행하면 목데이터가 사용하는 테이블을 모두 비우고 고정 ID로 다시 채웁니다. 로컬/dev DB 외에는 실행하지 않습니다.
- 계정 4개(`mock1@gmail.com` ~ `mock4@gmail.com`, 비밀번호 `pwpwpwpw`)로 로그인해 화면을 확인할 수 있습니다.
