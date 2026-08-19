-- 로컬/dev DB 화면 테스트용 목데이터.
-- Flyway가 자동 실행하지 않는다. 필요할 때 mysql 클라이언트 등으로 직접 실행한다.
--   mysql -h <host> -u <user> -p <database> < db/mock/mock.sql
-- 모든 목데이터 테이블을 비우고 고정 ID로 다시 채우므로, 로컬/dev DB 외에는 실행하지 않는다.
-- 계정 4개는 mock1@gmail.com ~ mock4@gmail.com, 비밀번호는 모두 pwpwpwpw (BCrypt 인코딩).

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE commit_connection;
TRUNCATE TABLE application_timeline;
TRUNCATE TABLE application_commits;
TRUNCATE TABLE application_base;
TRUNCATE TABLE application;
TRUNCATE TABLE decision_timeline;
TRUNCATE TABLE decision_commits;
TRUNCATE TABLE decision_base;
TRUNCATE TABLE decision;
TRUNCATE TABLE meeting_analysis;
TRUNCATE TABLE dialogue;
TRUNCATE TABLE meeting_member;
TRUNCATE TABLE meeting;
TRUNCATE TABLE commit_analysis;
TRUNCATE TABLE changed_file;
TRUNCATE TABLE commits;
TRUNCATE TABLE repository;
TRUNCATE TABLE team_member;
TRUNCATE TABLE team;
TRUNCATE TABLE member;

SET FOREIGN_KEY_CHECKS = 1;

-- member (비밀번호: pwpwpwpw)
INSERT INTO member (member_id, created_at, updated_at, name, email, password, role) VALUES
    (1, NOW(), NOW(), '목데이터1', 'mock1@gmail.com', '$2a$10$dnlVdvAaeUpcta/kmlVfO.bJfuauckS1ne6j6sSjyUa8j.BOsDTL2', 'USER'),
    (2, NOW(), NOW(), '목데이터2', 'mock2@gmail.com', '$2a$10$dnlVdvAaeUpcta/kmlVfO.bJfuauckS1ne6j6sSjyUa8j.BOsDTL2', 'USER'),
    (3, NOW(), NOW(), '목데이터3', 'mock3@gmail.com', '$2a$10$dnlVdvAaeUpcta/kmlVfO.bJfuauckS1ne6j6sSjyUa8j.BOsDTL2', 'USER'),
    (4, NOW(), NOW(), '목데이터4', 'mock4@gmail.com', '$2a$10$dnlVdvAaeUpcta/kmlVfO.bJfuauckS1ne6j6sSjyUa8j.BOsDTL2', 'USER');

-- team
INSERT INTO team (team_id, created_at, updated_at, name, image) VALUES
    (1, NOW(), NOW(), 'WhyLog 테스트팀', NULL);

INSERT INTO team_member (team_id, member_id, created_at, updated_at, is_active, role) VALUES
    (1, 1, NOW(), NOW(), 1, 'OWNER'),
    (1, 2, NOW(), NOW(), 1, 'MEMBER'),
    (1, 3, NOW(), NOW(), 1, 'MEMBER'),
    (1, 4, NOW(), NOW(), 1, 'MEMBER');

-- repository & commits (10건)
INSERT INTO repository (repository_id, created_at, updated_at, name, url, last_synced_at, team_id) VALUES
    (1, NOW(), NOW(), 'whylog-server', 'https://github.com/WhyLog-App/whylog-server', NOW(), 1);

INSERT INTO commits (commit_id, created_at, updated_at, repository_id, hash, message, author_name, author_email, author_profile_image, datetime, added_lines, deleted_lines) VALUES
    (1, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000001', 'feat: 로그인 기능 추가', '목데이터1', 'mock1@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 5 DAY), 120, 5),
    (2, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000002', 'fix: 회의 종료 시간 버그 수정', '목데이터2', 'mock2@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 5 DAY), 30, 12),
    (3, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000003', 'refactor: 결정 근거 조회 쿼리 정리', '목데이터3', 'mock3@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 4 DAY), 60, 40),
    (4, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000004', 'feat: 커밋 분석 임베딩 파이프라인 추가', '목데이터4', 'mock4@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 3 DAY), 200, 10),
    (5, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000005', 'fix: JWT 리프레시 토큰 만료 처리 수정', '목데이터1', 'mock1@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 3 DAY), 45, 20),
    (6, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000006', 'feat: 팀 초대 API 추가', '목데이터2', 'mock2@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 2 DAY), 90, 3),
    (7, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000007', 'perf: 회의 요약 캐싱 적용', '목데이터3', 'mock3@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 2 DAY), 70, 15),
    (8, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000008', 'fix: GitHub 웹훅 서명 검증 버그 수정', '목데이터4', 'mock4@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 1 DAY), 25, 8),
    (9, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000009', 'chore: Flyway 마이그레이션 도입', '목데이터1', 'mock1@gmail.com', 'https://placehold.co/64x64', DATE_SUB(NOW(), INTERVAL 1 DAY), 350, 0),
    (10, NOW(), NOW(), 1, 'a1b2c3d4e5f60000000000000000000000000010', 'docs: PR 리뷰 기록 문서화', '목데이터2', 'mock2@gmail.com', 'https://placehold.co/64x64', NOW(), 80, 2);

INSERT INTO changed_file (changed_file_id, created_at, updated_at, commit_id, file_name) VALUES
    (1, NOW(), NOW(), 1, 'src/main/java/com/whylog/server/domain/user/service/LocalLoginService.java'),
    (2, NOW(), NOW(), 1, 'src/main/java/com/whylog/server/domain/user/controller/AuthController.java'),
    (3, NOW(), NOW(), 2, 'src/main/java/com/whylog/server/domain/meeting/entity/Meeting.java'),
    (4, NOW(), NOW(), 3, 'src/main/java/com/whylog/server/domain/decision/repository/DecisionRepository.java'),
    (5, NOW(), NOW(), 4, 'src/main/java/com/whylog/server/domain/git/service/CommitAnalysisService.java'),
    (6, NOW(), NOW(), 4, 'src/main/java/com/whylog/server/domain/git/entity/CommitAnalysis.java'),
    (7, NOW(), NOW(), 5, 'src/main/java/com/whylog/server/global/util/jwt/JwtProvider.java'),
    (8, NOW(), NOW(), 6, 'src/main/java/com/whylog/server/domain/team/service/TeamCommandService.java'),
    (9, NOW(), NOW(), 7, 'src/main/java/com/whylog/server/domain/meeting/service/MeetingAnalysisService.java'),
    (10, NOW(), NOW(), 8, 'src/main/java/com/whylog/server/global/external/github/GithubWebhookVerifier.java'),
    (11, NOW(), NOW(), 9, 'src/main/resources/db/migration/V1__init_schema.sql'),
    (12, NOW(), NOW(), 10, 'docs/pr-reviews/2026-08.md');

INSERT INTO commit_analysis (commit_Analysis_id, created_at, updated_at, commit_id, summary, embedding_ready) VALUES
    (1, NOW(), NOW(), 1, '로그인 API에 이메일/비밀번호 검증 로직을 추가했다.', 1),
    (2, NOW(), NOW(), 3, '결정 근거 조회 시 N+1이 발생하던 쿼리를 fetch join으로 정리했다.', 1),
    (3, NOW(), NOW(), 4, '커밋 메시지를 벡터로 변환해 저장하는 임베딩 파이프라인을 추가했다.', 1),
    (4, NOW(), NOW(), 7, '회의 요약 결과를 Redis에 캐싱해 반복 조회 비용을 줄였다.', 1),
    (5, NOW(), NOW(), 9, 'Flyway 마이그레이션과 mock 데이터 스크립트를 도입했다.', 0);

-- meeting (종료된 회의 2건)
INSERT INTO meeting (meeting_id, created_at, updated_at, team_id, name, start_date_time, end_date_time, audio_key, is_normally_ended) VALUES
    (1, NOW(), NOW(), 1, '주간 스프린트 회의', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR, NULL, 1),
    (2, NOW(), NOW(), 1, '결정 근거 회고 회의', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 45 MINUTE, NULL, 1);

INSERT INTO meeting_member (meeting_id, member_id, created_at, updated_at, role) VALUES
    (1, 1, NOW(), NOW(), 'OWNER'),
    (1, 2, NOW(), NOW(), 'GENERAL'),
    (1, 3, NOW(), NOW(), 'GENERAL'),
    (2, 1, NOW(), NOW(), 'OWNER'),
    (2, 3, NOW(), NOW(), 'GENERAL'),
    (2, 4, NOW(), NOW(), 'GENERAL');

INSERT INTO dialogue (dialogue_id, created_at, updated_at, meeting_id, member_id, content, speech_datetime) VALUES
    (1, NOW(), NOW(), 1, 1, '오늘은 로그인 기능 마무리하고 회의 종료 버그를 같이 볼게요.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 MINUTE),
    (2, NOW(), NOW(), 1, 2, '회의 종료 시간이 UTC로 저장돼서 어긋나는 것 같아요.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 6 MINUTE),
    (3, NOW(), NOW(), 1, 1, 'JPA 설정에 타임존을 명시하지 않은 게 원인 같습니다.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 11 MINUTE),
    (4, NOW(), NOW(), 1, 3, '결정 근거 조회 쿼리도 N+1이 나서 같이 정리하면 좋겠어요.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 16 MINUTE),
    (5, NOW(), NOW(), 1, 2, '그럼 이번 스프린트 최우선 과제로 올릴게요.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 21 MINUTE),
    (6, NOW(), NOW(), 1, 1, '로그인 API는 오늘 리뷰 요청 올리겠습니다.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 26 MINUTE),
    (7, NOW(), NOW(), 1, 3, '좋아요, 정리되면 다음 회의 때 결과 공유할게요.', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 31 MINUTE),
    (8, NOW(), NOW(), 2, 1, '지난 회의에서 결정한 커밋 추천 정확도를 다시 점검해봤어요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 1 MINUTE),
    (9, NOW(), NOW(), 2, 4, '커밋 메시지만으로는 관련 없는 커밋도 추천되는 경우가 있더라고요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 7 MINUTE),
    (10, NOW(), NOW(), 2, 3, '임베딩 기반으로 유사도를 계산하면 나아질 것 같아요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 13 MINUTE),
    (11, NOW(), NOW(), 2, 1, '회의 요약도 매번 다시 계산돼서 느려요. 캐싱을 붙이면 좋겠어요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 19 MINUTE),
    (12, NOW(), NOW(), 2, 4, '그럼 임베딩 파이프라인이랑 캐싱을 이번 주 안에 붙여볼게요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 25 MINUTE),
    (13, NOW(), NOW(), 2, 3, '다음 회고 때 추천 정확도 변화도 같이 확인해봐요.', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE);

INSERT INTO meeting_analysis (meeting_analysis_id, created_at, updated_at, meeting_id, meeting_title, meeting_purpose, meeting_duration, analysis_content, topics, core_context, application_titles, application_reasons) VALUES
    (1, NOW(), NOW(), 1, '주간 스프린트 회의', '스프린트 진행 상황 공유 및 이슈 논의', '60분',
     '로그인 기능, 회의 종료 시간 버그, 결정 근거 조회 성능 문제를 논의했다.',
     '["로그인 기능","회의 종료 버그","쿼리 성능"]',
     '["로그인 API 검증 로직 추가","회의 종료 시간 타임존 문제","N+1 쿼리 정리"]',
     '["로그인 기능 적용","회의 종료 시간 수정","조회 쿼리 리팩터링"]',
     '["보안 요구사항 충족을 위해","타임존 버그로 인한 데이터 불일치 방지를 위해","조회 성능 개선을 위해"]'),
    (2, NOW(), NOW(), 2, '결정 근거 회고 회의', '커밋 추천 정확도 회고 및 개선 방향 논의', '45분',
     '커밋 추천 정확도 문제의 원인을 임베딩 부재로 진단하고, 임베딩 파이프라인과 회의 요약 캐싱 도입을 결정했다.',
     '["추천 정확도","임베딩","캐싱"]',
     '["키워드 매칭만으로는 관련 없는 커밋이 추천됨","임베딩 유사도 기반 추천 필요","회의 요약 재계산 비용 문제"]',
     '["임베딩 파이프라인 적용","회의 요약 캐싱 적용"]',
     '["추천 정확도 개선을 위해","반복 조회 비용 절감을 위해"]');

-- decision & applications
INSERT INTO decision (decision_id, created_at, updated_at, meeting_id, is_created, reliability_score) VALUES
    (1, NOW(), NOW(), 1, 1, 85),
    (2, NOW(), NOW(), 2, 1, 78);

INSERT INTO decision_base (decision_base_pk, created_at, updated_at, decision_id, content) VALUES
    (1, NOW(), NOW(), 1, '로그인 기능은 이번 스프린트 내 배포한다.'),
    (2, NOW(), NOW(), 1, '회의 종료 시간 버그와 결정 근거 조회 성능 문제를 이번 스프린트 최우선으로 수정한다.'),
    (3, NOW(), NOW(), 2, '커밋 추천 정확도를 높이기 위해 임베딩 파이프라인과 회의 요약 캐싱을 우선 적용한다.');

INSERT INTO decision_commits (decision_commits_pk, created_at, updated_at, decision_id, commit_id) VALUES
    (1, NOW(), NOW(), 1, 1),
    (2, NOW(), NOW(), 1, 2),
    (3, NOW(), NOW(), 1, 3),
    (4, NOW(), NOW(), 2, 4),
    (5, NOW(), NOW(), 2, 5),
    (6, NOW(), NOW(), 2, 7),
    (7, NOW(), NOW(), 2, 8);

INSERT INTO decision_timeline (decision_timeline_pk, created_at, updated_at, decision_id, timestamp, step, content, member_id, utterance) VALUES
    (1, NOW(), NOW(), 1, '00:01:00', '문제 제기', '로그인 기능과 회의 종료 버그를 같이 보기로 했다.', 1, '오늘은 로그인 기능 마무리하고 회의 종료 버그를 같이 볼게요.'),
    (2, NOW(), NOW(), 1, '00:06:00', '문제 제기', '회의 종료 시간이 어긋나는 문제를 공유했다.', 2, '회의 종료 시간이 UTC로 저장돼서 어긋나는 것 같아요.'),
    (3, NOW(), NOW(), 1, '00:11:00', '원인 분석', '타임존 설정 누락이 원인으로 지목됐다.', 1, 'JPA 설정에 타임존을 명시하지 않은 게 원인 같습니다.'),
    (4, NOW(), NOW(), 1, '00:16:00', '원인 분석', '결정 근거 조회 쿼리의 N+1 문제가 함께 제기됐다.', 3, '결정 근거 조회 쿼리도 N+1이 나서 같이 정리하면 좋겠어요.'),
    (5, NOW(), NOW(), 1, '00:21:00', '결정', '이번 스프린트에서 최우선으로 수정하기로 했다.', 2, '그럼 이번 스프린트 최우선 과제로 올릴게요.'),
    (6, NOW(), NOW(), 2, '00:01:00', '문제 제기', '커밋 추천 정확도를 다시 점검했다.', 1, '지난 회의에서 결정한 커밋 추천 정확도를 다시 점검해봤어요.'),
    (7, NOW(), NOW(), 2, '00:07:00', '원인 분석', '키워드 매칭만으로는 관련 없는 커밋도 추천된다는 점이 지적됐다.', 4, '커밋 메시지만으로는 관련 없는 커밋도 추천되는 경우가 있더라고요.'),
    (8, NOW(), NOW(), 2, '00:13:00', '대안 검토', '임베딩 기반 유사도 계산이 대안으로 제시됐다.', 3, '임베딩 기반으로 유사도를 계산하면 나아질 것 같아요.'),
    (9, NOW(), NOW(), 2, '00:25:00', '결정', '임베딩 파이프라인과 캐싱을 이번 주 안에 적용하기로 했다.', 4, '그럼 임베딩 파이프라인이랑 캐싱을 이번 주 안에 붙여볼게요.');

INSERT INTO application (application_id, created_at, updated_at, decision_id, name) VALUES
    (1, NOW(), NOW(), 1, '로그인 기능 배포'),
    (2, NOW(), NOW(), 1, '회의 종료 시간 버그 수정'),
    (3, NOW(), NOW(), 1, '결정 근거 조회 쿼리 리팩터링'),
    (4, NOW(), NOW(), 2, '커밋 분석 임베딩 파이프라인 적용'),
    (5, NOW(), NOW(), 2, '회의 요약 캐싱 적용');

INSERT INTO application_base (application_id, decision_base_pk, created_at, updated_at) VALUES
    (1, 1, NOW(), NOW()),
    (2, 2, NOW(), NOW()),
    (3, 2, NOW(), NOW()),
    (4, 3, NOW(), NOW()),
    (5, 3, NOW(), NOW());

INSERT INTO application_commits (application_id, decision_commits_pk, created_at, updated_at, reason, confidence) VALUES
    (1, 1, NOW(), NOW(), '로그인 API 구현 커밋이 결정 내용과 직접 연관된다.', 90),
    (2, 2, NOW(), NOW(), '회의 종료 시간 버그 수정 커밋이 결정 내용과 직접 연관된다.', 88),
    (3, 3, NOW(), NOW(), '결정 근거 조회 쿼리 리팩터링 커밋이 결정 내용과 직접 연관된다.', 84),
    (4, 4, NOW(), NOW(), '임베딩 파이프라인 구현 커밋이 결정 내용과 직접 연관된다.', 92),
    (5, 6, NOW(), NOW(), '회의 요약 캐싱 적용 커밋이 결정 내용과 직접 연관된다.', 86);

INSERT INTO application_timeline (application_id, decision_timeline_pk, created_at, updated_at) VALUES
    (1, 2, NOW(), NOW()),
    (2, 3, NOW(), NOW()),
    (3, 4, NOW(), NOW()),
    (4, 7, NOW(), NOW()),
    (5, 8, NOW(), NOW());

INSERT INTO commit_connection (application_id, commit_id, created_at, updated_at) VALUES
    (1, 1, NOW(), NOW()),
    (2, 2, NOW(), NOW()),
    (3, 3, NOW(), NOW()),
    (4, 4, NOW(), NOW()),
    (5, 7, NOW(), NOW());
