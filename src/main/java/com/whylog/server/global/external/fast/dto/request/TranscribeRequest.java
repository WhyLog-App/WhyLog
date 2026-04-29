package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.whylog.server.global.external.fast.client.FastApiBinaryPart;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "FastAPI 전사 요청")
public record TranscribeRequest(
        @Schema(description = "전사할 오디오 파일")
        FastApiBinaryPart audio,
        @Schema(description = "화자 수", nullable = true)
        Integer numSpeakers,
        @Schema(description = "회의 ID(선택)", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID(선택)", nullable = true)
        String projectId
) {
}
