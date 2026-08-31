package com.whylog.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthResponse {

    public enum LoginStatus {
        AUTHENTICATED,
        RECOVERY_REQUIRED,
        EMAIL_VERIFICATION_REQUIRED
    }

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

        @Schema(description = "로그인 처리 상태", example = "AUTHENTICATED")
        private LoginStatus status;

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

        @Schema(description = "탈퇴 복구 챌린지. RECOVERY_REQUIRED 상태에서만 내려갑니다.", nullable = true)
        private String withdrawalRecoveryChallenge;

        @Schema(description = "탈퇴 유예 종료 시각. RECOVERY_REQUIRED 상태에서만 내려갑니다.", nullable = true)
        private LocalDateTime purgeAt;

        public static LoginResponseDTO of(
                String accessToken, String refreshToken, Long memberId, String email, String role) {
            return LoginResponseDTO.builder()
                    .status(LoginStatus.AUTHENTICATED)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .build();
        }

        public static LoginResponseDTO emailVerificationRequired(
                Long memberId, String email, String role) {
            return LoginResponseDTO.builder()
                    .status(LoginStatus.EMAIL_VERIFICATION_REQUIRED)
                    .accessToken(null)
                    .refreshToken(null)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .build();
        }

        public static LoginResponseDTO recoveryRequired(
                Long memberId,
                String email,
                String role,
                String withdrawalRecoveryChallenge,
                LocalDateTime purgeAt) {
            return LoginResponseDTO.builder()
                    .status(LoginStatus.RECOVERY_REQUIRED)
                    .accessToken(null)
                    .refreshToken(null)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .withdrawalRecoveryChallenge(withdrawalRecoveryChallenge)
                    .purgeAt(purgeAt)
                    .build();
        }

        public boolean isAuthenticated() {
            return status == LoginStatus.AUTHENTICATED;
        }

        public LoginResponseDTO withoutRefreshToken() {
            return LoginResponseDTO.builder()
                    .status(status)
                    .accessToken(accessToken)
                    .refreshToken(null)
                    .memberId(memberId)
                    .email(email)
                    .role(role)
                    .withdrawalRecoveryChallenge(withdrawalRecoveryChallenge)
                    .purgeAt(purgeAt)
                    .build();
        }
    }
}
