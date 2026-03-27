package com.whylog.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회원가입 응답")
    public static class SignUpResponseDTO {

        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "로그인 응답")
    public static class LoginResponseDTO {

        @Schema(description = "액세스 토큰", example = "accessstokenenenen...")
        private String accessToken;

        @Schema(description = "회원 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;
    }
}
