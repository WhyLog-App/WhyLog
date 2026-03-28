package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingUseCase {

    private final MeetingRepository meetingRepository;

    // 회의 참여자 수
    public int getMeetingMemberCount(Meeting meeting){
        return meeting.getMeetingMembers().size();
    }

    // 회의의 참여자 정보
    public List<Member> getParticipantsInfo(Meeting meeting){
        return meeting.getMeetingMembers().stream()
                .map(MeetingMember::getMember)
                .toList();
    }

}
