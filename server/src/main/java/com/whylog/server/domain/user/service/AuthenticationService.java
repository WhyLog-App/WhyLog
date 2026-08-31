package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.AccessTokenGenerateResponse;
import com.whylog.server.domain.user.dto.AuthResponse;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.domain.user.exception.AuthErrorCode;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.auth.jwt.application.TokenService;
import com.whylog.server.global.auth.jwt.provider.JwtTokenProvider;
import com.whylog.server.global.auth.jwt.provider.JwtValidationType;
import com.whylog.server.global.auth.security.MemberAuthentication;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final MemberAccountStatusQueryService memberAccountStatusQueryService;

    @Transactional
    public AuthResponse.LoginResponseDTO generateLoginResponse(Member member) {
        Role role = member.getRole();
        Collection<GrantedAuthority> authorities = List.of(role.toGrantedAuthority());
        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(member.getId(), role, authorities);

        String refreshToken = jwtTokenProvider.issueRefreshToken(authentication);
        tokenService.saveRefreshToken(member.getId(), refreshToken);

        String accessToken = jwtTokenProvider.issueAccessToken(authentication);
        return AuthResponse.LoginResponseDTO.of(
                accessToken, refreshToken, member.getId(), member.getEmail(), role.getRoleName());
    }

    @Transactional(readOnly = true)
    public AccessTokenGenerateResponse generateAccessTokenFromRefreshToken(String refreshToken) {
        JwtValidationType validationType = jwtTokenProvider.validateToken(refreshToken);
        if (validationType != JwtValidationType.VALID_JWT) {
            throw new ErrorHandler(
                    switch (validationType) {
                        case EXPIRED_JWT_TOKEN -> AuthErrorCode.REFRESH_TOKEN_EXPIRED;
                        case INVALID_JWT_TOKEN,
                                INVALID_JWT_SIGNATURE,
                                UNSUPPORTED_JWT_TOKEN,
                                EMPTY_JWT ->
                                AuthErrorCode.INVALID_REFRESH_TOKEN;
                        default -> AuthErrorCode.INVALID_REFRESH_TOKEN;
                    });
        }

        Long memberId = jwtTokenProvider.getMemberIdFromJwt(refreshToken);
        Long storedMemberId = tokenService.findIdByRefreshToken(refreshToken);
        if (!memberId.equals(storedMemberId)) {
            throw new ErrorHandler(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (!memberAccountStatusQueryService.isActive(memberId)) {
            throw new ErrorHandler(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Role role = jwtTokenProvider.getRoleFromJwt(refreshToken);
        Collection<GrantedAuthority> authorities = List.of(role.toGrantedAuthority());
        UsernamePasswordAuthenticationToken authentication =
                createAuthentication(memberId, role, authorities);
        return AccessTokenGenerateResponse.from(jwtTokenProvider.issueAccessToken(authentication));
    }

    private UsernamePasswordAuthenticationToken createAuthentication(
            Long memberId, Role role, Collection<GrantedAuthority> authorities) {
        return new MemberAuthentication(memberId, null, authorities);
    }
}
