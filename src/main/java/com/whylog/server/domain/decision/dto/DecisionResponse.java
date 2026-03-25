package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DecisionResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결정사항 목록 조회 응답")
    public static class DecisionListDTO {

        @Schema(description = "결정사항 ID", example = "1")
        private Long decisionId;

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        private String name;

        @Schema(description = "적용사항 개수", example = "3")
        private Long applicationCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "신뢰도 조회 응답")
    public static class ReliabilityDTO {

        @Schema(description = "신뢰도 점수", example = "85")
        private Integer score;

        @Schema(description = "근거발언 개수", example = "42")
        private Integer reasonSpeechCount;

        @Schema(description = "참여자 합의도", example = "HIGH")
        private String participantConsensus;

        @Schema(description = "결정 구현 일치율", example = "92")
        private Integer matchRatio;
    }

}
