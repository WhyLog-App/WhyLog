package com.whylog.server.domain.meeting.socket.message;

// 잘못된 요청이나 처리 실패를 클라이언트에 전달하는 에러 메시지입니다.
public record ErrorMessage(
        MeetingMessageType type,
        String message
) {
}
