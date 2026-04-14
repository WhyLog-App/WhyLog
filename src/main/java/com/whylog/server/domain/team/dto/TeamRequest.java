package com.whylog.server.domain.team.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
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
        @Email @NotBlank
        private String memberEmail;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "팀 생성")
    public static class TeamCreateDTO {

        @Schema(description = "팀명 - 50글자 이내", example = "팀명이어떻게다마고치")
        @NotBlank
        private String name;

    }

}
