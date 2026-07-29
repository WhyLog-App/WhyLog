package com.whylog.server.domain.meeting.socket.message;

// 참가자 퇴장을 알리는 서버 메시지입니다.
public record ParticipantLeftMessage(
        MeetingMessageType type,
        Long meetingId,
        Long memberId,
        String name,
        String timestamp
) {
}
