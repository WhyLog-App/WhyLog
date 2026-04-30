package com.whylog.server.domain.meeting.socket.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// meetingId 기준으로 실시간 회의방 저장소를 관리하는 메모리 레포지토리입니다.
@Repository
public class MeetingSocketRoomRepository {

    // meetingId -> 해당 회의방의 실시간 참가자 저장소
    private final Map<Long, MeetingRoomRepository> rooms = new ConcurrentHashMap<>();

    // 회의방이 이미 있으면 반환하고, 없으면 새로 생성해서 반환합니다.
    public MeetingRoomRepository getOrCreate(Long meetingId) {
        return rooms.computeIfAbsent(meetingId, ignored -> new MeetingRoomRepository());
    }

    // 메모리에 올라와 있는 회의방 저장소를 조회합니다.
    public MeetingRoomRepository findByMeetingId(Long meetingId) {
        return rooms.get(meetingId);
    }

    // 회의방 저장소를 제거합니다.
    public void delete(Long meetingId) {
        rooms.remove(meetingId);
    }

    // 현재 메모리에 존재하는 회의방 id 목록을 반환합니다.
    public List<Long> findAllMeetingIds() {
        return new ArrayList<>(rooms.keySet());
    }
}
