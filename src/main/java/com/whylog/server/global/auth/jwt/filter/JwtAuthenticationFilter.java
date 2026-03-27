package com.whylog.server.global.auth.jwt.filter;

import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.global.auth.jwt.provider.JwtTokenProvider;
import com.whylog.server.global.auth.jwt.provider.JwtValidationType;
import com.whylog.server.global.auth.security.MemberAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = getJwtFromRequest(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        JwtValidationType validationType = jwtTokenProvider.validateToken(token);
        if (validationType != JwtValidationType.VALID_JWT) {
            response.setStatus(
                    validationType == JwtValidationType.EXPIRED_JWT_TOKEN
                            ? HttpServletResponse.SC_UNAUTHORIZED
                            : HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        setAuthentication(token, request);
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        Long memberId = jwtTokenProvider.getMemberIdFromJwt(token);
        Role role = jwtTokenProvider.getRoleFromJwt(token);

        Collection<GrantedAuthority> authorities = List.of(role.toGrantedAuthority());
        UsernamePasswordAuthenticationToken authentication = new MemberAuthentication(memberId, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
