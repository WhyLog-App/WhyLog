package com.whylog.server.global.config;

import com.whylog.server.domain.meeting.socket.MeetingSocketAuthInterceptor;
import com.whylog.server.domain.meeting.socket.MeetingSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

// 회의용 웹소켓 엔드포인트와 핸드셰이크 인터셉터를 등록하는 설정 클래스입니다.
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MeetingSocketHandler meetingSocketHandler;
    private final MeetingSocketAuthInterceptor meetingSocketAuthInterceptor;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    // 회의 웹소켓 핸들러를 `/ws/meetings` 경로에 등록하고 CORS 및 인증 인터셉터를 연결합니다.
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] originPatterns = Arrays.stream(allowedOrigins)
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        registry.addHandler(meetingSocketHandler, "/ws/meetings")
                .addInterceptors(meetingSocketAuthInterceptor)
                .setAllowedOriginPatterns(originPatterns);
    }
}
