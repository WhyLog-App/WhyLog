package com.whylog.server.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "회원가입 요청")
    public static class SignUpDTO {

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "비밀번호", example = "wtf1234")
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "로그인 요청")
    public static class LoginDTO {

        @Schema(description = "이메일", example = "user@example.com")
        private String email;

        @Schema(description = "비밀번호", example = "wtf1234")
        private String password;
    }
}
