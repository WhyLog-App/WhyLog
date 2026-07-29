package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "FastAPI 적용사항-커밋 추천 매칭 요청")
public record CommitMatchRequest(
        @Schema(description = "회의 ID", example = "1")
        String meetingId,
        @Schema(description = "후보로 사용할 레포지토리 ID 목록", example = "[1, 2]")
        List<Long> repositoryIds,
        @Schema(description = "적용사항별 최대 추천 개수", example = "5")
        Integer topK
) {
}
