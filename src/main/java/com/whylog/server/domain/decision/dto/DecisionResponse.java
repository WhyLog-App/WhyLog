package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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

        @Schema(description = "적용사항 목록")
        private List<ApplicationResponse.ApplicationDTO> applications;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "신뢰도 조회 응답")
    public static class ReliabilityDTO {

        @Schema(description = "신뢰도 점수", example = "85")
        private Integer score;
    }

}
