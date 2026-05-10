package com.whylog.server.global.external.fast.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "FastAPI 커밋 분석 요청")
public record CommitAnalyzeRequest(
        @Schema(description = "커밋 ID", nullable = true)
        Integer commitId,
        @Schema(description = "Git 커밋 해시")
        String commitHash,
        @Schema(description = "Spring 레포지토리 ID")
        Integer repositoryId,
        @Schema(description = "커밋 메시지")
        String message,
        @Schema(description = "변경된 파일 목록")
        List<ChangedFile> changedFileList
) {
}
