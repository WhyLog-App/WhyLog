package com.whylog.server.domain.user.controller;

import com.whylog.server.domain.user.dto.AccessTokenGenerateResponse;
import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.dto.AuthResponse;
import com.whylog.server.domain.user.exception.AuthErrorCode;
import com.whylog.server.domain.user.exception.AuthSuccessStatus;
import com.whylog.server.domain.user.service.AuthenticationService;
import com.whylog.server.domain.user.service.EmailVerificationCommandService;
import com.whylog.server.domain.user.service.LocalLoginService;
import com.whylog.server.domain.user.service.WithdrawalRecoveryCommandService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import com.whylog.server.global.auth.jwt.application.RefreshTokenCookieService;
import com.whylog.server.global.auth.jwt.application.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private static final String REFRESH_TOKEN = "refreshToken";
    private final LocalLoginService localLoginService;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final EmailVerificationCommandService emailVerificationCommandService;
    private final WithdrawalRecoveryCommandService withdrawalRecoveryCommandService;

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입 API",
            description =
                    """

            요청 형식:
            - `email`: 필수값이며 빈 문자열일 수 없습니다.
            - `email`: 이메일 형식을 만족해야 합니다. 예: `user@example.com`

            처리 방식:
            - 동일 이메일이 이미 존재하면 회원가입에 실패합니다.
            - 회원은 이메일 인증 전 상태로 생성되며 토큰은 발급하지 않습니다.
            - 최초 인증 메일 발송이 실패해도 회원가입은 롤백하지 않습니다.
            - 메일이 오지 않으면 같은 이메일 인증 코드 발급 API로 재발급할 수 있습니다.
            """)
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "EMAIL_ALREADY_EXISTS"),
        @ApiErrorCodeExample(
                value = AuthErrorCode.class,
                name = "EMAIL_VERIFICATION_DELIVERY_FAILED")
    })
    public ApiResponse<AuthResponse.SignUpResponseDTO> signup(
            @Valid @RequestBody AuthRequest.SignUpDTO request) {
        AuthResponse.SignUpResponseDTO response = localLoginService.signUp(request);
        return ApiResponse.of(AuthSuccessStatus.SIGN_UP_SUCCESS, response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "로그인 API",
            description =
                    """

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
            - 응답 바디에는 refresh token을 담지 않습니다.

            TTL 정책:
            - 액세스 토큰 TTL: 1시간 (`3600000ms`)
            - 리프레시 토큰 JWT TTL: 14일 (`1209600000ms`)
            - 리프레시 토큰 쿠키 Max-Age: 리프레시 토큰 JWT TTL과 동일

            탈퇴 유예 계정:
            - 30일 유예 중인 계정은 `RECOVERY_REQUIRED` 상태와 복구 챌린지를 반환합니다.
            - 이 경우 access token, refresh token, refresh cookie는 발급하지 않습니다.
            """)
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "LOGIN_FAILED")
    })
    public ApiResponse<AuthResponse.LoginResponseDTO> login(
            @Valid @RequestBody AuthRequest.LoginDTO request,
            HttpServletResponse httpServletResponse) {
        AuthResponse.LoginResponseDTO response = localLoginService.login(request);
        if (response.isAuthenticated()) {
            refreshTokenCookieService.write(httpServletResponse, response.getRefreshToken());
        }
        return ApiResponse.of(AuthSuccessStatus.LOGIN_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/email-verifications")
    @Operation(summary = "이메일 인증 코드 발급 API", description = "인증 전 회원에게 6자리 이메일 인증 코드를 발급하거나 재발급합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(
                value = AuthErrorCode.class,
                name = "EMAIL_VERIFICATION_DELIVERY_FAILED"),
        @ApiErrorCodeExample(
                value = AuthErrorCode.class,
                name = "EMAIL_VERIFICATION_ISSUE_COOLDOWN")
    })
    public ApiResponse<Void> issueEmailVerification(
            @Valid @RequestBody AuthRequest.EmailVerificationIssueDTO request) {
        emailVerificationCommandService.issue(request);
        return ApiResponse.of(AuthSuccessStatus.EMAIL_VERIFICATION_ISSUE_SUCCESS, null);
    }

    @PostMapping("/email-verifications/verify")
    @Operation(
            summary = "이메일 인증 코드 검증 API",
            description = "6자리 이메일 인증 코드를 확인하고 인증 완료 후 로그인 응답을 반환합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "EMAIL_VERIFICATION_CODE_INVALID"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "EMAIL_VERIFICATION_CODE_EXPIRED")
    })
    public ApiResponse<AuthResponse.LoginResponseDTO> verifyEmailVerification(
            @Valid @RequestBody AuthRequest.EmailVerificationVerifyDTO request,
            HttpServletResponse httpServletResponse) {
        AuthResponse.LoginResponseDTO response =
                authenticationService.generateLoginResponse(
                        emailVerificationCommandService.verify(request));
        refreshTokenCookieService.write(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(
                AuthSuccessStatus.EMAIL_VERIFICATION_VERIFY_SUCCESS,
                response.withoutRefreshToken());
    }

    @PostMapping("/withdrawal-recoveries/verify")
    @Operation(summary = "탈퇴 유예 계정 복구 API", description = "탈퇴 유예 로그인에서 받은 일회성 챌린지를 확인하고 계정을 복구합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(
                value = AuthErrorCode.class,
                name = "WITHDRAWAL_RECOVERY_CHALLENGE_INVALID")
    })
    public ApiResponse<AuthResponse.LoginResponseDTO> verifyWithdrawalRecovery(
            @Valid @RequestBody AuthRequest.WithdrawalRecoveryVerifyDTO request,
            HttpServletResponse httpServletResponse) {
        AuthResponse.LoginResponseDTO response =
                authenticationService.generateLoginResponse(
                        withdrawalRecoveryCommandService.verify(request));
        refreshTokenCookieService.write(httpServletResponse, response.getRefreshToken());
        return ApiResponse.of(
                AuthSuccessStatus.WITHDRAWAL_RECOVERY_SUCCESS, response.withoutRefreshToken());
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "액세스 토큰 재발급 API",
            description =
                    """

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
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "INVALID_REFRESH_TOKEN"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "REFRESH_TOKEN_EXPIRED"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "REFRESH_TOKEN_NOT_FOUND")
    })
    public ApiResponse<AccessTokenGenerateResponse> refreshToken(
            @CookieValue(REFRESH_TOKEN) String refreshToken) {
        return ApiResponse.of(
                AuthSuccessStatus.REFRESH_TOKEN_SUCCESS,
                authenticationService.generateAccessTokenFromRefreshToken(refreshToken));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃 API",
            description =
                    """

            요청 방식:
            - `Authorization: Bearer {access_token}` 헤더가 필요합니다.
            - 서버는 액세스 토큰에서 현재 사용자 식별자를 추출합니다.
            - 요청 바디는 사용하지 않습니다.

            처리 방식:
            - 저장소에 보관 중인 리프레시 토큰을 삭제합니다.
            - 응답 시 `refreshToken` 쿠키를 만료 처리합니다.

            쿠키 처리 방식:
            - `Set-Cookie: refreshToken=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax` 형식으로 만료합니다.
            - 운영 환경에서는 설정에 따라 Secure 속성이 함께 붙을 수 있습니다.
            - 브라우저 기준으로 refresh token 쿠키가 제거됩니다.
            """)
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = AuthErrorCode.class, name = "REFRESH_TOKEN_NOT_FOUND")
    })
    public ApiResponse<Void> logout(
            @CurrentMember Long memberId, HttpServletResponse httpServletResponse) {
        tokenService.deleteRefreshToken(memberId);
        refreshTokenCookieService.expire(httpServletResponse);
        return ApiResponse.of(AuthSuccessStatus.LOGOUT_SUCCESS, null);
    }
}
