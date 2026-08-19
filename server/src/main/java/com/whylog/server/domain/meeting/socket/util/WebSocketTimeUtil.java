package com.whylog.server.domain.meeting.socket.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

// 웹소켓 연산 담당 클래스
public class WebSocketTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final String ZERO_ELAPSED = "00:00:00";

    // 웹소켓 메시지에 사용할 현재 시각 문자열을 생성합니다.
    public static String now() {
        return ZonedDateTime.now(ZoneId.systemDefault()).format(FORMATTER);
    }

    // 회의 시작 시각 대비 경과 시간을 FastAPI가 쓰는 HH:MM:SS 형식으로 만듭니다.
    public static String formatElapsed(LocalDateTime startDateTime, LocalDateTime target) {
        try {
            if (startDateTime == null || target == null) {
                return ZERO_ELAPSED;
            }

            long elapsedSeconds = Duration.between(startDateTime, target).getSeconds();
            if (elapsedSeconds < 0) {
                return ZERO_ELAPSED;
            }

            long hours = elapsedSeconds / 3600;
            long minutes = (elapsedSeconds % 3600) / 60;
            long seconds = elapsedSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (RuntimeException exception) {
            return ZERO_ELAPSED;
        }
    }
}
