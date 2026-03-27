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
    @Operation(summary = "회원가입 API",
            description = """

            요청 형식:
            - `email`: 필수값이며 빈 문자열일 수 없습니다.
            - `email`: 이메일 형식을 만족해야 합니다. 예: `user@example.com`

            처리 방식:
            - 동일 이메일이 이미 존재하면 회원가입에 실패합니다.
            - 회원 생성 직후 자동 로그인 처리되어 액세스 토큰과 리프레시 토큰이 함께 발급됩니다.

            토큰 응답 방식:
            - 액세스 토큰은 응답 바디로 반환됩니다.
            - 리프레시 토큰은 응답 바디에도 존재하지만, 실제 클라이언트 사용은 `Set-Cookie` 헤더의 HttpOnly 쿠키(`refreshToken`) 기준입니다.
            - refresh token 쿠키는 `HttpOnly`, `Path=/`, `SameSite=Lax` 속성으로 내려갑니다.

            TTL 정책:
            - 액세스 토큰 TTL: 1시간 (`3600000ms`)
            - 리프레시 토큰 JWT TTL: 14일 (`1209600000ms`)
            - 리프레시 토큰 쿠키 Max-Age: 7일
            
            그래도...
            - 쿠키로 처리하기 귀찮거나...번거로울 경우를 대비해 Refresh Token을 Response Body에도 담아 보냅니다...😎
            """)
    public ApiResponse<AuthResponse.LoginResponseDTO> signup(
            @Valid @RequestBody AuthRequest.SignUpDTO request,
            HttpServletResponse httpServletResponse
    ) {
        AuthResponse.LoginResponseDTO response = localLoginService.signUp(request);
        writeRefreshTokenCookie(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(AuthSuccessStatus.SIGN_UP_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = """

            요청 형식:
            - `email`: 필수값이며 빈 문자열일 수 없습니다.
            - `email`: 이메일 형식을 만족해야 합니다.
            - `password`: 필수값이며 빈 문자열일 수 없습니다.

            처리 방식:
            - 이메일로 회원을 조회한 뒤 비밀번호를 검증합니다.
            - 이메일 또는 비밀번호가 올바르지 않으면 로그인에 실패합니다.

            토큰 응답 방식:
            - 액세스 토큰은 응답 바디로 반환됩니다.
            - 리프레시 토큰은 `Set-Cookie` 헤더를 통해 HttpOnly 쿠키(`refreshToken`)로 저장됩니다.
            - refresh token 쿠키는 브라우저가 이후 요청에 자동 포함하도록 설계되어 있습니다.

            TTL 정책:
            - 액세스 토큰 TTL: 1시간 (`3600000ms`)
            - 리프레시 토큰 JWT TTL: 14일 (`1209600000ms`)
            - 리프레시 토큰 쿠키 Max-Age: 7일
            
            그래도...
            - 쿠키로 처리하기 귀찮거나...번거로울 경우를 대비해 Refresh Token을 Response Body에도 담아 보냅니다...😎
            """)
    public ApiResponse<AuthResponse.LoginResponseDTO> login(
            @Valid @RequestBody AuthRequest.LoginDTO request,
            HttpServletResponse httpServletResponse
    ) {
        AuthResponse.LoginResponseDTO response = localLoginService.login(request);
        writeRefreshTokenCookie(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(AuthSuccessStatus.LOGIN_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "액세스 토큰 재발급 API", description = """

            요청 방식:
            - 요청 바디는 사용하지 않습니다.
            - 브라우저 또는 클라이언트는 쿠키에 저장된 `refreshToken`을 함께 전송해야 합니다.
            - 서버는 `Cookie` 헤더에서 `refreshToken` 값을 읽습니다.

            요청 예시:
            - `POST /api/auth/refresh-token`
            - `Cookie: refreshToken={refresh_token}`

            검증 방식:
            - 리프레시 토큰의 서명/형식/만료 여부를 검증합니다.
            - 저장소에 보관된 리프레시 토큰과 사용자 식별자가 일치하는지 검증합니다.

            응답 방식:
            - 새 액세스 토큰만 응답 바디로 반환합니다.
            - 리프레시 토큰은 이 API에서 재발급하지 않습니다.

            TTL 정책:
            - 새 액세스 토큰 TTL: 1시간 (`3600000ms`)
            - 기존 리프레시 토큰 JWT TTL: 14일 (`1209600000ms`)
            """)
    public ApiResponse<AccessTokenGenerateResponse> refreshToken(
            @CookieValue(REFRESH_TOKEN) String refreshToken
    ) {
        return ApiResponse.of(
                AuthSuccessStatus.REFRESH_TOKEN_SUCCESS,
                authenticationService.generateAccessTokenFromRefreshToken(refreshToken)
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = """

            요청 방식:
            - `Authorization: Bearer {access_token}` 헤더가 필요합니다.
            - 서버는 액세스 토큰에서 현재 사용자 식별자를 추출합니다.
            - 요청 바디는 사용하지 않습니다.

            처리 방식:
            - 저장소에 보관 중인 리프레시 토큰을 삭제합니다.
            - 응답 시 `refreshToken` 쿠키를 만료 처리합니다.

            쿠키 처리 방식:
            - `Set-Cookie: refreshToken=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax`
            - 브라우저 기준으로 refresh token 쿠키가 제거됩니다.
            """)
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
