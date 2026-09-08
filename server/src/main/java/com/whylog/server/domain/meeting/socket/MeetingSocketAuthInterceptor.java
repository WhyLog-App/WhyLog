package com.whylog.server.domain.meeting.socket;

import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.auth.jwt.provider.JwtTokenProvider;
import com.whylog.server.global.auth.jwt.provider.JwtValidationType;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

// 웹소켓 핸드셰이크 시 쿼리 파라미터의 회의 ID와 JWT를 검증하고 세션 속성에 담습니다.
@Component
@RequiredArgsConstructor
public class MeetingSocketAuthInterceptor implements HandshakeInterceptor {

    public static final String MEETING_ID_ATTRIBUTE = "meetingId";
    public static final String MEMBER_ID_ATTRIBUTE = "memberId";
    public static final String MEMBER_NAME_ATTRIBUTE = "memberName";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberUseCase memberUseCase;
    private final MeetingSocketRoomService meetingSocketRoomService;

    // 웹소켓 연결 전에 meetingId, accessToken, 표시 이름을 확인하고 세션 속성을 초기화합니다.
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        MultiValueMap<String, String> queryParams =
                UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();

        Long meetingId = parseMeetingId(queryParams.getFirst("meetingId"));
        String token = decode(queryParams.getFirst("accessToken"));

        if (meetingId == null || !StringUtils.hasText(token)) {
            setStatus(response, HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        JwtValidationType validationType = jwtTokenProvider.validateToken(token);
        if (validationType != JwtValidationType.VALID_JWT) {
            setStatus(response, HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Long memberId = jwtTokenProvider.getMemberIdFromJwt(token);
        Member member = memberUseCase.findMemberById(memberId);
        if (!member.getAccountStatus().canUseNormalService()) {
            setStatus(response, HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        attributes.put(MEETING_ID_ATTRIBUTE, meetingId);
        attributes.put(MEMBER_ID_ATTRIBUTE, memberId);
        attributes.put(MEMBER_NAME_ATTRIBUTE, resolveName(queryParams, member));
        return true;
    }

    // 핸드셰이크 이후 추가 작업은 없어서 비워 둡니다.
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {}

    // 문자열 meetingId를 Long 타입으로 안전하게 변환합니다.
    private Long parseMeetingId(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    // 클라이언트가 넘긴 이름이 있으면 사용하고, 없으면 회원 이메일을 기본 이름으로 사용합니다.
    private String resolveName(MultiValueMap<String, String> queryParams, Member member) {
        return Optional.ofNullable(queryParams.getFirst("name"))
                .map(this::decode)
                .filter(StringUtils::hasText)
                .orElse(member.getName());
    }

    // URL 인코딩된 쿼리 파라미터 값을 디코딩합니다.
    private String decode(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    // 핸드셰이크 실패 시 HTTP 상태 코드를 응답에 기록합니다.
    private void setStatus(ServerHttpResponse response, int statusCode) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            servletResponse.getServletResponse().setStatus(statusCode);
        }
    }
}
