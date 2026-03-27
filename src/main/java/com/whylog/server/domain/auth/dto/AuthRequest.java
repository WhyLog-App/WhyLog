package com.whylog.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "회원가입 요청")
    public static class SignUpDTO {

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email
        private String email;

        @Schema(description = "비밀번호", example = "wtf1234")
        @NotBlank
        private String password;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "로그인 요청")
    public static class LoginDTO {

        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email
        private String email;

        @Schema(description = "비밀번호", example = "wtf1234")
        @NotBlank
        private String password;
    }
}
