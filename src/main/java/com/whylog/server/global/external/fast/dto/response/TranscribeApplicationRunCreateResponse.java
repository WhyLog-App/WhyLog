package com.whylog.server.global.external.fast.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "FastAPI 전사+회의 분석 비동기 실행 생성 응답")
public record TranscribeApplicationRunCreateResponse(
        @Schema(description = "비동기 실행 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String runId,
        @Schema(description = "실행 상태", example = "queued")
        String status,
        @Schema(description = "실행 단계", example = "queued")
        String phase,
        @Schema(description = "회의 ID", example = "meeting-123", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID", example = "project-abc", nullable = true)
        String projectId
) {
}
