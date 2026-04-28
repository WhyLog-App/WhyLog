package com.whylog.server.global.external.fast.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FastAPI 회의 분석 요청")
public record MeetingAnalysisRequest(
        @Schema(description = "회의 ID(선택)", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID(선택)", nullable = true)
        String projectId,
        @Schema(description = "전사 세그먼트 배열")
        JsonNode transcriptSegments
) {
}
