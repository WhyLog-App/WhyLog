package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.meeting.exception.MeetingErrorCode;
import com.whylog.server.domain.meeting.exception.MeetingAlreadyEndedException;
import com.whylog.server.domain.meeting.exception.MeetingInvalidMemberException;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.meeting.service.MeetingAnalysisService;
import com.whylog.server.domain.meeting.service.MeetingCleanupService;
import com.whylog.server.domain.meeting.service.LiveKitTokenService;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.service.TeamUseCase;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.external.livekit.LiveKitEgressClient;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingCommandService {

    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingCleanupService meetingCleanupService;
    private final MeetingAudioFileService meetingAudioFileService;
    private final MeetingAnalysisService meetingAnalysisService;
    private final LiveKitTokenService liveKitTokenService;
    private final LiveKitEgressClient liveKitEgressClient;

    private final MemberUseCase memberUseCase;
    private final TeamUseCase teamUseCase;

    private final MeetingSocketRoomService meetingSocketRoomService;

    /*
        회의를 생성합니다.

        회의를 생성하여 endDateTime이 null인 회의를 저장합니다.
        - endDateTime == null : 진행 중인 회의를 의미합니다.

        회의와 회의참여자 정보가 함께 저장됩니다.
     */
    @Transactional
    public MeetingResponse.MeetingCreateResponseDTO makeMeetingRoom(Long memberId, Long teamId, MeetingRequest.MeetingCreateDTO requestDTO){

        // null 체크
        if(teamId == null) throw new ParameterRequiredException();

        // API 호출한 유저 정보 조회
        Member member = memberUseCase.findMemberById(memberId);

        // 팀 조회
        Team team = teamUseCase.findTeamById(teamId);

        // Meeting, MeetingMember 같이 저장
        Meeting meeting = Meeting.create(requestDTO, team);
        Meeting savedMeeting = meetingRepository.save(meeting);
        String audioKey = meetingAudioFileService.buildRecordingKey(savedMeeting.getId());
        savedMeeting.setAudioKey(audioKey);
        meetingRepository.save(savedMeeting);
        MeetingMember meetingMember = MeetingMember.create(savedMeeting, member, MeetingRole.OWNER);
        meetingMemberRepository.save(meetingMember);

        // 실시간 회의 추가
        meetingSocketRoomService.createRoomIfAbsent(savedMeeting.getId()); // 현재 참여자 추가

        // dto 생성 후 반환
        return MeetingResponse.MeetingCreateResponseDTO.builder()
                .meetingId(savedMeeting.getId())
                .name(savedMeeting.getName())
                .startDateTime(savedMeeting.getStartDateTime())
                .build();
    }

    /*
        회의를 종료합니다.
        - 실시간 회의 참여자들에게 회의 종료를 메시지를 보냅니다.
        - 실시간 데이터로 다루는 실시간 회의 정보도 제거합니다.
     */
    @Transactional
    public MeetingResponse.MeetingEndResponseDTO endMeeting(Long memberId, Long meetingId) {

        // 조회 및 검증
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        if(!meetingMemberRepository.existsByMemberIdAndMeetingId(memberId, meetingId)) // 회의 참여자 존재 검증
            throw new MeetingInvalidMemberException();

        if (!meeting.isOngoing()) { // 이미 종료된 회의인지 검증
            throw new MeetingAlreadyEndedException();
        }

        return finishMeeting(meeting, true);
    }

    @Transactional
    public void autoEndMeetingIfEmpty(Long meetingId) {
        if (!meetingSocketRoomService.listParticipants(meetingId).isEmpty()) {
            return;
        }

        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null || !meeting.isOngoing()) {
            return;
        }

        finishMeeting(meeting, false);
    }

    @Transactional
    public MeetingResponse.MeetingDeleteResponseDTO deleteMeeting(Long memberId, Long meetingId) {

        meetingRepository.findById(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        meetingMemberRepository.findOwnerMeetingMember(memberId, meetingId, MeetingRole.OWNER)
                .orElseThrow(() -> new ErrorHandler(MeetingErrorCode.MEETING_NOT_OWNER));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
        stopRecording(meeting);
        meetingCleanupService.deleteByMeetingId(meetingId);
        scheduleAfterCommit(() -> meetingSocketRoomService.closeRoom(meetingId));

        return MeetingResponse.MeetingDeleteResponseDTO.builder()
                .meetingId(meetingId)
                .isRemoved(true)
                .build();
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }

        task.run();
    }

    private void stopRecording(Meeting meeting) {
        if (meeting == null || meeting.getAudioEgressId() == null || meeting.getAudioEgressId().isBlank()) {
            return;
        }

        String roomName = "meeting-" + meeting.getId();
        String egressToken = liveKitTokenService.createRoomRecordToken("recording-" + meeting.getId(), roomName);
        liveKitEgressClient.stopEgress(egressToken, meeting.getAudioEgressId());
    }

    private MeetingResponse.MeetingEndResponseDTO finishMeeting(Meeting meeting, boolean broadcastEnded) {
        LocalDateTime endDateTime = meeting.endMeeting();
        meetingRepository.save(meeting);

        if (broadcastEnded) {
            meetingSocketRoomService.broadcastMeetingEnded(meeting.getId(), endDateTime);
        }

        stopRecording(meeting);
        meetingSocketRoomService.closeRoom(meeting.getId());

        scheduleAfterCommit(() -> CompletableFuture.runAsync(() -> meetingAnalysisService.analyzeMeetingAudio(meeting.getId()))
                .exceptionally(ex -> {
                    log.error("회의 오디오 분석 실패: meetingId={}", meeting.getId(), ex);
                    return null;
                }));

        return MeetingResponse.MeetingEndResponseDTO.builder()
                .meetingId(meeting.getId())
                .endDateTime(endDateTime)
                .build();
    }

}
