package com.whylog.server.global.external.fast.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "FastAPI 회의 분석 임베딩 응답")
public record ApplicationEmbeddingsResponse(
        @Schema(description = "회의 ID", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID", nullable = true)
        String projectId,
        @Schema(description = "문서 수", nullable = true)
        Integer totalDocuments,
        @Schema(description = "문서 ID 목록", nullable = true)
        List<String> documentIds,
        @Schema(description = "문서 목록", nullable = true)
        List<DocumentResponse> documents
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "임베딩 문서")
    public record DocumentResponse(
            @Schema(description = "문서 ID", nullable = true)
            String documentId,
            @Schema(description = "문서 본문", nullable = true)
            String text,
            @Schema(description = "적용사항 ID", nullable = true)
            Long applicationId,
            @Schema(description = "적용사항 제목", nullable = true)
            String applicationTitle
    ) {
    }
}
