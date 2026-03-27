package com.whylog.server.domain.user.controller;

import com.whylog.server.domain.user.dto.AccessTokenGenerateResponse;
import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.dto.AuthResponse;
import com.whylog.server.domain.user.service.AuthenticationService;
import com.whylog.server.domain.user.service.LocalLoginService;
import com.whylog.server.domain.user.exception.AuthSuccessStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import com.whylog.server.global.auth.jwt.application.TokenService;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private static final String REFRESH_TOKEN = "refreshToken";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    private final LocalLoginService localLoginService;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입 API", description = "이메일과 비밀번호를 이용하여 새로운 회원을 등록하는 API입니다.")
    public ApiResponse<AuthResponse.LoginResponseDTO> signup(
            @Valid @RequestBody AuthRequest.SignUpDTO request,
            HttpServletResponse httpServletResponse
    ) {
        AuthResponse.LoginResponseDTO response = localLoginService.signUp(request);
        writeRefreshTokenCookie(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(AuthSuccessStatus.SIGN_UP_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "이메일과 비밀번호를 이용하여 로그인하고 액세스 토큰을 발급받는 API입니다.")
    public ApiResponse<AuthResponse.LoginResponseDTO> login(
            @Valid @RequestBody AuthRequest.LoginDTO request,
            HttpServletResponse httpServletResponse
    ) {
        AuthResponse.LoginResponseDTO response = localLoginService.login(request);
        writeRefreshTokenCookie(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(AuthSuccessStatus.LOGIN_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "액세스 토큰 재발급 API", description = "리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    public ApiResponse<AccessTokenGenerateResponse> refreshToken(
            @CookieValue(REFRESH_TOKEN) String refreshToken
    ) {
        return ApiResponse.of(
                AuthSuccessStatus.REFRESH_TOKEN_SUCCESS,
                authenticationService.generateAccessTokenFromRefreshToken(refreshToken)
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "현재 사용자의 리프레시 토큰을 삭제합니다.")
    public ApiResponse<Void> logout(
            @CurrentMember Long memberId,
            HttpServletResponse httpServletResponse
    ) {
        tokenService.deleteRefreshToken(memberId);
        expireRefreshTokenCookie(httpServletResponse);
        return ApiResponse.of(AuthSuccessStatus.LOGOUT_SUCCESS, null);
    }

    private void writeRefreshTokenCookie(HttpServletResponse httpServletResponse, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .maxAge(COOKIE_MAX_AGE)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    private void expireRefreshTokenCookie(HttpServletResponse httpServletResponse) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN, "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }
}
