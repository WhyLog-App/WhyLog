package com.whylog.server.domain.meeting.socket.repository;

import com.whylog.server.domain.meeting.socket.MeetingParticipant;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 하나의 회의방에 연결된 웹소켓 참가자 세션들을 메모리에서 관리합니다.
public class MeetingRoomRepository {

    // sessionId -> MeetingParticipantSession
    private final Map<String, MeetingParticipant> participants = new ConcurrentHashMap<>();

    // 새로 연결된 참가자 세션을 회의방에 추가합니다.
    public void addParticipant(MeetingParticipant participant) {
        participants.put(participant.sessionId(), participant);
    }

    // 연결이 종료된 참가자 세션을 회의방에서 제거합니다.
    public MeetingParticipant removeParticipant(String sessionId) {
        return participants.remove(sessionId);
    }

    // 현재 회의방에 연결된 전체 참가자 세션 목록을 반환합니다.
    public Collection<MeetingParticipant> participants() {
        return participants.values();
    }

    // 회의방에 남아 있는 참가자가 없는지 확인합니다.
    public boolean isEmpty() {
        return participants.isEmpty();
    }
}
