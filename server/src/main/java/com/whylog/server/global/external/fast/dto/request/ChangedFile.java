package com.whylog.server.global.external.fast.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FastAPI 커밋 변경 파일")
public record ChangedFile(
        @Schema(description = "변경된 파일 경로")
        String fileName,
        @Schema(description = "unified diff 형식의 변경 코드")
        String changedCode
) {
}
