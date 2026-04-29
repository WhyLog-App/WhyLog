package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingUseCase meetingUseCase;
    private final MeetingAudioReplayService meetingAudioReplayService;

    // 미팅 목록 조회
    @Transactional(readOnly = true)
    public List<MeetingResponse.MeetingListDTO> getMeetings(Long teamId, MeetingStatus status){

        // 기본값: 진행완료
        MeetingStatus targetStatus = status != null ? status : MeetingStatus.COMPLETED;

        List<Meeting> meetings = meetingUseCase.findMeetingByTeamId(teamId);

        return meetings.stream()
                .filter( m -> checkMeetingStatus(m, targetStatus)) // 상태 일치 체크
                .map(m -> MeetingResponse.MeetingListDTO.builder()
                        .meetingId(m.getId())
                        .name(m.getName())
                        .status(m.getStatus())
                        .elapse( !m.isOngoing() ? m.getElapse() : null ) // 진행완료일 경우 null로 반환
                        .build()
                ).toList();

    }

    // 회의 기본 정보 조회
    @Transactional(readOnly = true)
    public MeetingResponse.MeetingDetailDTO getMeetingDefaultInfo(Long meetingId){

        if (meetingId == null) { // null check
            throw new ParameterRequiredException();
        }

        Meeting meeting = meetingUseCase.findMeetingById(meetingId);

        return MeetingResponse.MeetingDetailDTO.builder()
                .meetingId(meeting.getId())
                .name(meeting.getName())
                .startDateTime(meeting.getStartDateTime())
                .endDateTime(meeting.getEndDateTime())
                .duration(meeting.getDuration())
                .memberCount( meetingUseCase.getMeetingMemberCount(meeting) )
                .members( memberToParticipantsInfo(meetingUseCase.getParticipantsInfo(meeting)) )
                .audioDuration( meetingAudioReplayService.resolveAudioDurationIfAvailable(meeting) )
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

    private boolean checkMeetingStatus(Meeting meeting, MeetingStatus status){
        return meeting.getStatus() == status;
    }

    @Transactional(readOnly = true)
    public MeetingResponse.AudioDTO getMeetingAudio(Long meetingId) {
        Meeting meeting = meetingUseCase.findMeetingById(meetingId);
        return meetingAudioReplayService.buildAudioResponse(meeting);
    }

    @Transactional(readOnly = true)
    public MeetingResponse.AnalysisResultDTO getAnalysis(Long meetingId) {

        meetingUseCase.findMeetingById(meetingId); // 없으면 그거에 따른 예외 발생

        MeetingAnalysis meetingAnalysis = meetingUseCase.findAnalysisByMeetingId(meetingId)
                .orElse(null);

        if(meetingAnalysis == null) // null이면 isAnalyzed = false인 응답 반환
            return MeetingResponse.AnalysisResultDTO.createFalse(meetingId);

        Integer audioDuration = meetingAudioReplayService.resolveAudioDurationIfAvailable(meetingAnalysis.getMeeting());
        return MeetingResponse.AnalysisResultDTO.create(meetingAnalysis, audioDuration);
    }

}
