package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ApplicationResponse {


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "적용사항 항목")
    public static class ApplicationDTO {

        @Schema(description = "적용사항 ID", example = "1")
        private Long applicationId;

        @Schema(description = "적용사항 명", example = "Redis키 정책 변경")
        private String name;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "적용사항 상세 조회 응답")
    public static class ApplicationDetailDTO {

        @Schema(description = "적용사항 ID", example = "1")
        private Long applicationId;

        @Schema(description = "적용사항 명", example = "Redis키 정책 변경")
        private String name;

        @Schema(description = "결정 타임라인 목록")
        private List<DecisionTimelineItemDTO> decisionTimelines;

        @Schema(description = "결정 원문 맥락 목록")
        private List<DecisionContextItemDTO> decisionContexts;

        @Schema(description = "결정근거 개수", example = "3")
        private Integer decisionReasonCount;

        @Schema(description = "결정근거 목록")
        private List<DecisionReasonItemDTO> decisionReasons;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정 타임라인 항목")
    public static class DecisionTimelineItemDTO {

        @Schema(description = "타임라인 시간", example = "2026-03-24T12:28:00")
        private String time;

        @Schema(description = "타임라인 단계", example = "이슈제기")
        private String step;

        @Schema(description = "타임라인 내용", example = "장애 이슈 제기")
        private String content;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정 원문 맥락 항목")
    public static class DecisionContextItemDTO {

        @Schema(description = "타임라인 시간", example = "2026-03-24T12:28:00")
        private String time;

        @Schema(description = "발화자 ID", example = "1", nullable = true)
        private Long memberId;

        @Schema(description = "발화자 이름", example = "김주뇽", nullable = true)
        private String memberName;

        @Schema(description = "발화자 프로필 사진", example = "https://example.com/profile.jpg", nullable = true)
        private String profileImage;

        @Schema(description = "대화 내용", example = "아니 우리 이거 버그난다니까?!?@??@")
        private String dialogueContent;
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정근거 항목")
    public static class DecisionReasonItemDTO {

        @Schema(description = "근거 ID", example = "1")
        private String reasonId;

        @Schema(description = "근거 내용", example = "운영복잡 우려로 보류")
        private String title;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "적용현황 조회 응답")
    public static class ApplicationStatusDTO {

        @Schema(description = "연결된 커밋 개수", example = "3")
        private Integer commitCount;

        @Schema(description = "적용현황 커밋 목록")
        private List<ApplicationBaseItemDTO> commits;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "적용현황 커밋 항목")
    public static class ApplicationBaseItemDTO {

        @Schema(description = "커밋 해시", example = "b8fd9ad")
        private String commitHash;

        @Schema(description = "커밋 메시지", example = "feat: API 구현")
        private String commitMessage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 커밋 응답")
    public static class RecommendedCommitDTO {

        @Schema(description = "저장소 이름", example = "whyLog-Backend")
        private String repositoryName;

        @Schema(description = "커밋 ID", example = "1")
        private String commitId;

        @Schema(description = "커밋 해시", example = "b8fd9ad")
        private String commitHash;

        @Schema(description = "커밋 메시지", example = "feat: API 구현")
        private String message;

        @Schema(description = "추천 사유", example = "이 커밋은 관련된 이슈를 해결하는 커밋입니다.")
        private String reason;

        @Schema(description = "추천 신뢰도", example = "94")
        private Integer confidence;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "연결된 커밋 목록 조회 응답")
    public static class ConnectedCommitListDTO {

        @Schema(description = "연결된 커밋 개수", example = "3")
        private Integer commitCount;

        @Schema(description = "연결된 커밋 목록")
        private List<ConnectedCommitDTO> commits;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "연결된 커밋 조회 응답")
    public static class ConnectedCommitDTO {

        @Schema(description = "저장소 이름", example = "whyLog-Backend")
        private String repositoryName;

        @Schema(description = "커밋 해시", example = "b8fd9ad")
        private String commitHash;

        @Schema(description = "커밋 메시지", example = "feat: API 구현")
        private String message;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime committedDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 연결 응답")
    public static class CommitConnectionResponseDTO {

        @Schema(description = "적용사항 ID", example = "1")
        private Long applicationId;

        @Schema(description = "연결된 커밋 ID 목록", example = "[1, 2, 3]")
        private List<Long> commitIds;
    }

}
