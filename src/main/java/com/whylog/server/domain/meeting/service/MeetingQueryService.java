package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingUseCase meetingUseCase;

    // 미팅 목록 조회
    @Transactional(readOnly = true)
    public List<MeetingResponse.MeetingListDTO> getMeetings(Long teamId, MeetingStatus status){

        // 기본값: 진행완료
        if (status == null) {
            status = MeetingStatus.COMPLETED;
        }

        List<Meeting> meetings = meetingRepository.findByTeamId(teamId);

        return meetings.stream()
                .map(m -> MeetingResponse.MeetingListDTO.builder()
                        .meetingId(m.getId())
                        .name(m.getName())
                        .status(m.getStatus())
                        .elapse( m.isOngoing() ? m.getElapse() : null ) // 진행완료일 경우 null로 반환
                        .build()
                ).toList();

    }

    // 회의 기본 정보 조회
    @Transactional(readOnly = true)
    public MeetingResponse.MeetingDetailDTO getMeetingDefaultInfo(Long meetingId){

        if (meetingId == null) { // null check
            throw new ParameterRequiredException();
        }

        Meeting meeting = meetingRepository.findWithMembers(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        return MeetingResponse.MeetingDetailDTO.builder()
                .meetingId(meeting.getId())
                .name(meeting.getName())
                .startDateTime(meeting.getStartDateTime())
                .endDateTime(meeting.getEndDateTime())
                .duration(meeting.getDuration())
                .memberCount( meetingUseCase.getMeetingMemberCount(meeting) )
                .members( memberToParticipantsInfo(meetingUseCase.getParticipantsInfo(meeting)) )
                .build();
    }

    private List<MeetingResponse.MeetingParticipantInfo> memberToParticipantsInfo(List<Member> members){
        return members.stream()
                .map(member -> MeetingResponse.MeetingParticipantInfo.builder()
                        .memberId(member.getId())
                        .name(member.getName())
                        .profileImage(member.getProfileImage())
                        .build()
                ).toList();
    }

}
