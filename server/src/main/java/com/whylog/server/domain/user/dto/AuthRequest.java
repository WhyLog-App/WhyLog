package com.whylog.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "회원가입 요청")
    public static class SignUpDTO {

        @Schema(description = "이름", example = "아무개")
        @NotBlank private String name;

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email @Size(max = 50) private String email;

        @Schema(description = "비밀번호", example = "wtf12345")
        @NotBlank @Size(min = 8, max = 100) private String password;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "로그인 요청")
    public static class LoginDTO {

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email @Size(max = 50) private String email;

        @Schema(description = "비밀번호", example = "wtf12345")
        @NotBlank private String password;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "이메일 인증 코드 발급 요청")
    public static class EmailVerificationIssueDTO {

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email @Size(max = 50) private String email;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "이메일 인증 코드 확인 요청")
    public static class EmailVerificationVerifyDTO {

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email @Size(max = 50) private String email;

        @Schema(description = "6자리 이메일 인증 코드", example = "123456")
        @NotBlank @Pattern(regexp = "\\d{6}", message = "이메일 인증 코드는 6자리 숫자여야 합니다.") private String code;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "탈퇴 유예 계정 복구 요청")
    public static class WithdrawalRecoveryVerifyDTO {

        @Schema(description = "멤버 ID", example = "1")
        @NotNull private Long memberId;

        @Schema(description = "탈퇴 복구 챌린지")
        @NotBlank private String challenge;
    }
}
