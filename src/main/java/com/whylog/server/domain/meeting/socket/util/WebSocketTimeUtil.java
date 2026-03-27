package com.whylog.server.domain.meeting.socket.util;

import java.time.Instant;

// 웹소켓 연산 담당 클래스
public class WebSocketTimeUtil {

    // 웹소켓 메시지에 사용할 현재 시각 문자열을 생성합니다.
    public static String now() {
        return Instant.now().toString();
    }

}
