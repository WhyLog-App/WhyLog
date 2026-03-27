package com.whylog.server.global.auth.jwt.provider;

import com.whylog.server.domain.user.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String MEMBER_ID = "memberId";
    private static final String ROLE = "role";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    @PostConstruct
    protected void init() {
        jwtSecret = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(Authentication authentication) {
        return issueToken(authentication, accessTokenExpireTime);
    }

    public String issueRefreshToken(Authentication authentication) {
        return issueToken(authentication, refreshTokenExpireTime);
    }

    public JwtValidationType validateToken(String token) {
        try {
            getBody(token);
            return JwtValidationType.VALID_JWT;
        } catch (MalformedJwtException ex) {
            return JwtValidationType.INVALID_JWT_TOKEN;
        } catch (ExpiredJwtException ex) {
            return JwtValidationType.EXPIRED_JWT_TOKEN;
        } catch (UnsupportedJwtException ex) {
            return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
        } catch (IllegalArgumentException ex) {
            return JwtValidationType.EMPTY_JWT;
        } catch (SignatureException ex) {
            return JwtValidationType.INVALID_JWT_SIGNATURE;
        }
    }

    public Long getMemberIdFromJwt(String token) {
        Claims claims = getBody(token);
        Object memberId = claims.get(MEMBER_ID);
        if (memberId != null) {
            return Long.valueOf(memberId.toString());
        }
        return Long.valueOf(claims.get(MEMBER_ID).toString());
    }

    public Role getRoleFromJwt(String token) {
        Claims claims = getBody(token);
        String roleName = claims.get(ROLE, String.class);
        return Role.valueOf(roleName.replace("ROLE_", ""));
    }

    private String issueToken(Authentication authentication, long expiredTime) {
        Date now = new Date();
        Claims claims = Jwts.claims()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiredTime));

        claims.put(MEMBER_ID, authentication.getPrincipal());
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No authorities found for member"));
        claims.put(ROLE, role);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(getSigningKey())
                .compact();
    }

    private Claims getBody(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        String encodedKey = Base64.getEncoder().encodeToString(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Keys.hmacShaKeyFor(encodedKey.getBytes(StandardCharsets.UTF_8));
    }
}
