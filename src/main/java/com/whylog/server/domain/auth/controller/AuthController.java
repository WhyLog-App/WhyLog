package com.whylog.server.domain.auth.controller;

import com.whylog.server.domain.auth.dto.AuthRequest;
import com.whylog.server.domain.auth.dto.AuthResponse;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    @PostMapping("/signup")
    @Operation(summary = "회원가입 API", description = "이메일과 비밀번호를 이용하여 새로운 회원을 등록하는 API입니다.")
    public ApiResponse<AuthResponse.SignUpResponseDTO> signup(@Valid @RequestBody AuthRequest.SignUpDTO request) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 이용하여 로그인하고 액세스 토큰을 발급받는 API입니다.")
    public ApiResponse<AuthResponse.LoginResponseDTO> login(@Valid @RequestBody AuthRequest.LoginDTO request) {
        return ApiResponse.onSuccess(null);
    }
}
