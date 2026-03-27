package com.whylog.server.domain.meeting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "회의 초대 요청", name = "MeetingInvitationDTO")
    public static class MeetingInvitationDTO {

        @Schema(description = "초대할 멤버 ID", example = "1")
        @NotNull
        private Long memberId;
    }

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

        @Schema(description = "회의 참여자 목록", example = "[1, 2, 3]")
        private List<Long> memberIds;

    }
}
