package com.whylog.server.domain.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class MeetingRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 생성 요청")
    public static class MeetingCreateDTO {

        @Schema(description = "회의 명", example = "백엔드 비상대책회의")
        @NotBlank
        private String name;

        @Schema(description = "시작 시간", example = "2026-03-24T10:00:00")
        private LocalDateTime startDateTime;

    }
}
