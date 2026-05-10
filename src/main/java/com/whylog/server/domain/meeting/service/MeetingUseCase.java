package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.exception.MeetingErrorCode;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingUseCase{

    private final MeetingRepository meetingRepository;

    public Meeting findMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(MeetingNotFoundException::new);
        checkMeeting(meeting);
        return meeting;
    }

    public Meeting findMeetingWithMembersById(Long id) {
        Meeting meeting = meetingRepository.findWithMembers(id)
                .orElseThrow(MeetingNotFoundException::new);
        checkMeeting(meeting);
        return meeting;
    }

    public List<Meeting> findMeetingByTeamId(Long teamId) {
        List<Meeting> meetings = meetingRepository.findWithAnalysis(teamId);
        checkMeeting(meetings);
        return meetings;
    }

    // 회의 참여자 수
    public int getMeetingMemberCount(Meeting meeting) {
        return meeting.getMeetingMembers().size();
    }

    // 회의의 참여자 정보
    public List<Member> getParticipantsInfo(Meeting meeting) {
        return meeting.getMeetingMembers().stream()
                .map(MeetingMember::getMember)
                .toList();
    }

    public Meeting findWithAnalysisByMeetingId(Long meetingId) {
        Meeting meeting = meetingRepository.findByMeetingId(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
        checkMeeting(meeting);
        return meeting;
    }

    public Meeting findWithDialogue(Long meetingId) {
        Meeting meeting = meetingRepository.findByMeetingId(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
        checkMeeting(meeting);
        return meeting;
    }

    private void checkMeeting(Meeting meeting) {
        if (meeting.getEndDateTime() == null) {
            throw new GeneralException(MeetingErrorCode.MEETING_NOT_END);
        }

        if ( !meeting.getIsNormallyEnded() ){
            throw new GeneralException(MeetingErrorCode.MEETING_UNNORMAL_END);
        }
    }

    private void checkMeeting(List<Meeting> meetings) {
        meetings.removeIf(meeting ->
                meeting.getEndDateTime() == null
                        || !meeting.getIsNormallyEnded()
        );
    }

}
