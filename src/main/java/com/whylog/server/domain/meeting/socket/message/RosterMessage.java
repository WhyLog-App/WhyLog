package com.whylog.server.domain.meeting.socket.message;

import com.whylog.server.domain.meeting.socket.util.WebSocketTimeUtil;

import java.util.List;

// 회의방의 최신 참가자 목록을 통째로 전달하는 서버 메시지입니다.
public record RosterMessage(
        MeetingMessageType type,
        Long meetingId,
        List<ParticipantSummary> participants,
        String timestamp
) {

    public static RosterMessage create(Long meetingId, List<ParticipantSummary> participants) {
        return new RosterMessage(
                MeetingMessageType.ROSTER,
                meetingId,
                participants,
                WebSocketTimeUtil.now()
        );
    }
}
