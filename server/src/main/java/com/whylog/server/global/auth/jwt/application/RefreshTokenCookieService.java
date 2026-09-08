package com.whylog.server.global.auth.jwt.application;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenCookieService {

    private static final String REFRESH_TOKEN = "refreshToken";

    private final Duration maxAge;
    private final boolean secure;

    public RefreshTokenCookieService(
            @Value("${jwt.refresh-token-expire-time}") long refreshTokenExpireTime,
            @Value("${REFRESH_TOKEN_COOKIE_SECURE:true}") boolean secure) {
        this.maxAge = Duration.ofMillis(refreshTokenExpireTime);
        this.secure = secure;
    }

    public void write(HttpServletResponse httpServletResponse, String refreshToken) {
        ResponseCookie cookie =
                ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                        .maxAge(maxAge)
                        .path("/")
                        .httpOnly(true)
                        .secure(secure)
                        .sameSite("Lax")
                        .build();
        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    public void expire(HttpServletResponse httpServletResponse) {
        ResponseCookie cookie =
                ResponseCookie.from(REFRESH_TOKEN, "")
                        .maxAge(Duration.ZERO)
                        .path("/")
                        .httpOnly(true)
                        .secure(secure)
                        .sameSite("Lax")
                        .build();
        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }
}
