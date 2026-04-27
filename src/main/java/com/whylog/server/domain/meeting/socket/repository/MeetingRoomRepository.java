package com.whylog.server.domain.meeting.socket.repository;

import com.whylog.server.domain.meeting.socket.MeetingParticipant;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// 하나의 회의방에 연결된 웹소켓 참가자 세션들을 메모리에서 관리합니다.
public class MeetingRoomRepository {

    // sessionId -> MeetingParticipantSession
    private final Map<String, MeetingParticipant> participantsBySessionId = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> sessionIdsByMemberId = new ConcurrentHashMap<>();

    // 새로 연결된 참가자 세션을 회의방에 추가합니다.
    public void addParticipant(MeetingParticipant participant) {
        participantsBySessionId.put(participant.sessionId(), participant);
        sessionIdsByMemberId.compute(participant.memberId(), (memberId, sessionIds) -> {
            Set<String> targetSessionIds = sessionIds != null ? sessionIds : ConcurrentHashMap.newKeySet();
            targetSessionIds.add(participant.sessionId());
            return targetSessionIds;
        });
    }

    // 연결이 종료된 참가자 세션을 회의방에서 제거합니다.
    public MeetingParticipant removeParticipant(String sessionId) {
        MeetingParticipant removed = participantsBySessionId.remove(sessionId);
        if (removed == null) {
            return null;
        }

        sessionIdsByMemberId.computeIfPresent(removed.memberId(), (memberId, sessionIds) -> {
            sessionIds.remove(sessionId);
            return sessionIds.isEmpty() ? null : sessionIds;
        });
        return removed;
    }

    // 현재 회의방에 연결된 전체 참가자 세션 목록을 반환합니다.
    public Collection<MeetingParticipant> participants() {
        return participantsBySessionId.values();
    }

    // 특정 멤버의 모든 연결 세션을 반환합니다.
    public List<MeetingParticipant> participantsByMemberId(Long memberId) {
        Set<String> sessionIds = sessionIdsByMemberId.get(memberId);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }

        return sessionIds.stream()
                .map(participantsBySessionId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    // 회의방에 남아 있는 참가자가 없는지 확인합니다.
    public boolean isEmpty() {
        return participantsBySessionId.isEmpty();
    }
}
