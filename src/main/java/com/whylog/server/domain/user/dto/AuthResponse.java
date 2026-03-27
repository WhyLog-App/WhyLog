package com.whylog.server.domain.user.dto;

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

        @Schema(description = "멤버 ID", example = "1")
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

        @Schema(description = "리프레시 토큰", example = "refreshtokenenenen...")
        private String refreshToken;

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "권한", example = "ROLE_USER")
        private String role;

        public static LoginResponseDTO of(
                String accessToken,
                String refreshToken,
                Long memberId,
                String email,
                String role
        ) {
            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .build();
        }

        public LoginResponseDTO withoutRefreshToken() {
            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .build();
        }
    }
}
