package com.whylog.server.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TeamResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "팀 초대 응답")
    public static class InvitationResponseDTO {

        @Schema(description = "팀 ID", example = "1")
        private Long teamId;

        @Schema(description = "초대받은 사용자 이메일", example = "member@example.com")
        private String memberEmail;
    }
}
