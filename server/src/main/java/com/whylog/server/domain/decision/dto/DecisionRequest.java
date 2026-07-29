package com.whylog.server.domain.decision.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

public class DecisionRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 연결 요청")
    public static class CommitConnectionDTO {

        @Schema(description = "연결할 커밋 ID 목록. 단건 연결도 배열로 전달합니다.", example = "[1, 2, 3]")
        @NotEmpty
        private List<@NotNull Long> commitIds;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 연결 해제 요청")
    public static class CommitDisconnectionDTO {

        @Schema(description = "해제할 커밋 ID", example = "1")
        @NotNull
        private Long commitId;
    }

}
