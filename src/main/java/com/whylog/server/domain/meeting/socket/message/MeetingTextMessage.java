package com.whylog.server.domain.meeting.socket.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import com.whylog.server.domain.meeting.socket.util.WebSocketTimeUtil;

// 채팅, 자막, 시그널링 등 텍스트 프레임 기반 서버 송신 메시지입니다.
public record MeetingTextMessage(
        MeetingMessageType type,
        Long meetingId,
        Long fromMemberId,
        String fromName,
        String timestamp,
        Long targetMemberId,
        String text,
        JsonNode payload
) {

    public static MeetingTextMessage createTextMessage(
            MeetingParticipant participant,
            MeetingMessageType type,
            Long targetMemberId,
            String text,
            JsonNode payload
    ) {
        return new MeetingTextMessage(
                type,
                participant.meetingId(),
                participant.memberId(),
                participant.name(),
                WebSocketTimeUtil.now(),
                targetMemberId,
                text,
                payload
        );
    }

}
