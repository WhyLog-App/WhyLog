package com.whylog.server.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class TeamRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "팀 초대 요청")
    public static class InvitationDTO {

        @Schema(description = "초대받을 사용자 이메일", example = "member@example.com")
        @NotBlank @Email
        private String memberEmail;
    }

}
