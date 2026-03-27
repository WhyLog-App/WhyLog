package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class DecisionRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 연결 요청")
    public static class CommitConnectionDTO {

        @Schema(description = "커밋 ID", example = "1")
        @NotBlank
        private Long commitId;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 결과 저장 요청")
    public static class RecommendationDTO {

        @Schema(description = "추천 커밋 ID", example = "1")
        @NotBlank
        private Long commitId;

        @Schema(description = "추천 이유", example = "이 커밋은 관련된 이슈를 해결하는 커밋입니다.")
        private String reason;
    }

}
