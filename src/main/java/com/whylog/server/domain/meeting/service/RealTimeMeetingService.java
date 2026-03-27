package com.whylog.server.domain.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 실시간 회의 정보입니다.
 */
@Service
@RequiredArgsConstructor
public class RealTimeMeetingService {

    // meetingId, memberId 순서쌍
    private final ConcurrentHashMap<Long, Long> currentMeetingMemberId = new ConcurrentHashMap<>();

    // 실시간 회의 팀원 참여
    public void addMember(Long meetingId, Long memberId) {
        // TODO: 실시간 회의 정보 업데이트 추가
    }

    // 실시간 회의 팀원 퇴장

    // 실시간 회의 종료


}
