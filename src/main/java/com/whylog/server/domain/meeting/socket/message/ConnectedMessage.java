package com.whylog.server.domain.meeting.socket.message;

import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import com.whylog.server.domain.meeting.socket.util.WebSocketTimeUtil;

import java.util.List;

// 웹소켓 연결 직후 현재 참가자 목록과 함께 전달하는 초기 응답입니다.
public record ConnectedMessage(
        MeetingMessageType type,
        Long meetingId,
        Long fromMemberId,
        String fromName,
        String timestamp,
        List<ParticipantSummary> participants
) {

    public static ConnectedMessage create(MeetingParticipant participant, List<ParticipantSummary> participantSummaries) {
        return new ConnectedMessage(
                MeetingMessageType.CONNECTED,
                participant.meetingId(),
                participant.memberId(),
                participant.name(),
                WebSocketTimeUtil.now(),
                participantSummaries
        );
    }

}
