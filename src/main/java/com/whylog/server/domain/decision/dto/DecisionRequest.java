package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DecisionRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 연결 요청")
    public static class CommitConnectionDTO {

        @Schema(description = "커밋 ID", example = "1")
        private String commitId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "추천 결과 저장 요청")
    public static class RecommendationDTO {

        @Schema(description = "추천 커밋 ID", example = "1")
        private String commitId;

        @Schema(description = "추천 이유", example = "이 커밋은 관련된 이슈를 해결하는 커밋입니다.")
        private String reason;
    }

}
