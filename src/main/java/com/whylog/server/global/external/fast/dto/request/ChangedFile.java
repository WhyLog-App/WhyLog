package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "FastAPI 커밋 변경 파일")
public record ChangedFile(
        @Schema(description = "변경된 파일 경로")
        String fileName,
        @Schema(description = "unified diff 형식의 변경 코드")
        String changedCode
) {
}
