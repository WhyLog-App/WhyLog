package com.whylog.server.domain.meeting.socket;

import org.springframework.web.socket.WebSocketSession;

// 웹소켓 세션 하나에 매핑되는 회의 참가자 정보를 담는 레코드입니다.
public record MeetingParticipant(
        String sessionId,
        Long memberId,
        String name,
        Long meetingId,
        WebSocketSession socketSession
) {
}
