package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MeetingUseCase {

    private final MeetingRepository meetingRepository;
    private final MeetingAnalysisRepository meetingAnalysisRepository;

    public Meeting findMeetingById(Long id){
        return meetingRepository.findById(id)
                .orElseThrow(MeetingNotFoundException::new);
    }

    public Meeting findMeetingWithMembersById(Long id){
        return meetingRepository.findWithMembers(id)
                .orElseThrow(MeetingNotFoundException::new);
    }

    public List<Meeting> findMeetingByTeamId(Long teamId){
        return meetingRepository.findByTeamId(teamId);
    }

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

    public Optional<MeetingAnalysis> findAnalysisByMeetingId(Long meetingId) {
        return meetingAnalysisRepository.findByMeetingId(meetingId);
    }

}
