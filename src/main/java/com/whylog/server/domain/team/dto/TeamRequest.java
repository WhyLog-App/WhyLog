package com.whylog.server.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TeamRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "팀 초대 요청")
    public static class InvitationDTO {

        @Schema(description = "초대받을 사용자 이메일", example = "member@example.com")
        private String memberEmail;
    }

}
