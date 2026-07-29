package com.whylog.server.domain.meeting.socket.message;

import com.fasterxml.jackson.databind.JsonNode;

// 클라이언트가 웹소켓 텍스트 프레임으로 보내는 회의 메시지 형식입니다.
public record MeetingSocketMessage(
        MeetingMessageType type,
        Long targetMemberId,
        String text,
        JsonNode payload
) {
}
