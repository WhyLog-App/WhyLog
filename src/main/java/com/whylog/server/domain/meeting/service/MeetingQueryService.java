package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.exception.MeetingAudioNotReadyException;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.external.s3.S3Client;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MeetingQueryService {

    private final MeetingUseCase meetingUseCase;
    private final MeetingAudioFileService meetingAudioFileService;
    private final MeetingAudioDurationService meetingAudioDurationService;
    private final S3Client s3Client;
    private final MeetingRepository meetingRepository;

    // 미팅 목록 조회
    @Transactional(readOnly = true)
    public List<MeetingResponse.MeetingListDTO> getMeetings(Long teamId, MeetingStatus status){

        // 기본값: 진행완료
        MeetingStatus targetStatus = status != null ? status : MeetingStatus.COMPLETED;

        List<Meeting> meetings = meetingRepository.findByTeamId(teamId);

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
                .audioDuration(resolveAudioDuration(resolveAudioKey(meeting)))
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
        String audioKey = resolveAudioKey(meeting);

        return MeetingResponse.AudioDTO.builder()
                .meetingId(meeting.getId())
                .audioKey(audioKey)
                .audioUrl(s3Client.getPresignedFileUrl(
                        audioKey,
                        Duration.ofMinutes(10),
                        meetingAudioFileService.resolveResponseContentType(audioKey)
                ))
                .audioDuration(resolveAudioDuration(audioKey))
                .build();
    }

    private String resolveAudioKey(Meeting meeting) {
        String audioKey = meeting.getAudioKey();
        if (isPlayableAudioKey(audioKey)) {
            return audioKey;
        }

        String alternateAudioKey = meetingAudioFileService.alternateKey(audioKey);
        if (isPlayableAudioKey(alternateAudioKey)) {
            return alternateAudioKey;
        }

        throw new MeetingAudioNotReadyException();
    }

    private boolean isPlayableAudioKey(String audioKey) {
        return audioKey != null && !audioKey.isBlank() && s3Client.exists(audioKey);
    }

    private Integer resolveAudioDuration(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            return null;
        }
        return meetingAudioDurationService.resolveAudioDurationSeconds(audioKey);
    }

}
