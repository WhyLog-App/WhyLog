package com.whylog.server.domain.meeting.socket.message;

import com.whylog.server.domain.meeting.socket.MeetingParticipant;

// 클라이언트에 노출할 최소 참가자 정보입니다.
public record ParticipantSummary(
        Long memberId,
        String name
) {

    public static ParticipantSummary create(MeetingParticipant participantRepository) {
        return new ParticipantSummary(participantRepository.memberId(), participantRepository.name());
    }

}
