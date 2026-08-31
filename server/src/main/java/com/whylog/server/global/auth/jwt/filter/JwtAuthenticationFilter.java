package com.whylog.server.global.auth.jwt.filter;

import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.domain.user.service.MemberAccountStatusQueryService;
import com.whylog.server.global.auth.jwt.provider.JwtTokenProvider;
import com.whylog.server.global.auth.jwt.provider.JwtValidationType;
import com.whylog.server.global.auth.security.MemberAuthentication;
import com.whylog.server.global.auth.security.PublicAuthPaths;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberAccountStatusQueryService memberAccountStatusQueryService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return isOptions(request) || isPublicAuthPost(request);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
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
                            : HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Long memberId = jwtTokenProvider.getMemberIdFromJwt(token);
        if (!memberAccountStatusQueryService.isActive(memberId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        setAuthentication(token, memberId, request);
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, Long memberId, HttpServletRequest request) {
        Role role = jwtTokenProvider.getRoleFromJwt(token);

        Collection<GrantedAuthority> authorities = List.of(role.toGrantedAuthority());
        UsernamePasswordAuthenticationToken authentication =
                new MemberAuthentication(memberId, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isOptions(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isPublicAuthPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        return PublicAuthPaths.contains(getRequestPath(request));
    }

    private String getRequestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (StringUtils.hasText(contextPath) && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
