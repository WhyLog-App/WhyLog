package com.whylog.server.domain.meeting.socket.message;

import java.time.LocalDateTime;

// 회의 종료를 알리는 서버 메시지입니다.
public record MeetingEndedMessage(
        MeetingMessageType type,
        Long meetingId,
        LocalDateTime endedAt
) {
}
