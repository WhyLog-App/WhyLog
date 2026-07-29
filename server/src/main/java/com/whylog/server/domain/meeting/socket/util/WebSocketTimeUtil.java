package com.whylog.server.domain.meeting.socket.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

// 웹소켓 연산 담당 클래스
public class WebSocketTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    // 웹소켓 메시지에 사용할 현재 시각 문자열을 생성합니다.
    public static String now() {
        return ZonedDateTime.now(ZoneId.systemDefault())
                .format(FORMATTER);
    }

}
