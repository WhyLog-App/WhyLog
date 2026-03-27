package com.whylog.server.domain.meeting.socket.message;

import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import java.time.Instant;

// 새 참가자가 회의방에 입장했음을 알리는 서버 메시지입니다.
public record ParticipantJoinedMessage(
        MeetingMessageType type,
        Long meetingId,
        Long memberId,
        String name,
        String timestamp
) {
    public static ParticipantJoinedMessage create(MeetingParticipant participant) {
        return new ParticipantJoinedMessage(
                MeetingMessageType.PARTICIPANT_JOINED,
                participant.meetingId(),
                participant.memberId(),
                participant.name(),
                Instant.now().toString());
    }
}
