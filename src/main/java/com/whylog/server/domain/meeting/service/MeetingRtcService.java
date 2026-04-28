package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.external.livekit.LiveKitEgressClient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeetingRtcService {

    private final MeetingRepository meetingRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MemberUseCase memberUseCase;
    private final MeetingAudioFileService meetingAudioFileService;
    private final LiveKitTokenService liveKitTokenService;
    private final LiveKitEgressClient liveKitEgressClient;
    private final String liveKitUrl;

    public MeetingRtcService(
            MeetingRepository meetingRepository,
            MeetingMemberRepository meetingMemberRepository,
            MemberUseCase memberUseCase,
            MeetingAudioFileService meetingAudioFileService,
            LiveKitTokenService liveKitTokenService,
            LiveKitEgressClient liveKitEgressClient,
            @Value("${livekit.url}") @NotBlank String liveKitUrl
    ) {
        this.meetingRepository = meetingRepository;
        this.meetingMemberRepository = meetingMemberRepository;
        this.memberUseCase = memberUseCase;
        this.meetingAudioFileService = meetingAudioFileService;
        this.liveKitTokenService = liveKitTokenService;
        this.liveKitEgressClient = liveKitEgressClient;
        this.liveKitUrl = liveKitUrl;
    }

    @Transactional
    public MeetingResponse.MeetingRtcTokenDTO issueRtcToken(Long memberId, Long meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        Member member = memberUseCase.findMemberById(memberId);
        ensureMeetingParticipant(meeting, member);
        String roomName = buildRoomName(meeting);
        startRecordingIfAbsent(meeting, roomName);
        String identity = String.valueOf(member.getId());
        String token = liveKitTokenService.createJoinToken(identity, member.getName(), roomName);

        return MeetingResponse.MeetingRtcTokenDTO.builder()
                .meetingId(meetingId)
                .roomName(roomName)
                .serverUrl(liveKitUrl)
                .token(token)
                .build();
    }

    private void ensureMeetingParticipant(Meeting meeting, Member member) {
        if (meetingMemberRepository.existsByMemberIdAndMeetingId(member.getId(), meeting.getId())) {
            return;
        }
        meetingMemberRepository.save(MeetingMember.create(meeting, member, MeetingRole.GENERAL));
    }

    private String buildRoomName(Meeting meeting) {
        return "meeting-" + meeting.getId();
    }

    private void startRecordingIfAbsent(Meeting meeting, String roomName) {
        if (meeting.getAudioEgressId() != null && !meeting.getAudioEgressId().isBlank()) {
            return;
        }

        String audioKey = meeting.getAudioKey();
        if (audioKey == null || audioKey.isBlank()) {
            audioKey = meetingAudioFileService.buildRecordingKey(meeting.getId());
            meeting.setAudioKey(audioKey);
        }

        String roomAdminToken = liveKitTokenService.createRoomCreateToken("room-admin");
        liveKitEgressClient.createRoom(roomAdminToken, roomName);

        String egressToken = liveKitTokenService.createRoomRecordToken("recording-" + meeting.getId(), roomName);
        String egressId = liveKitEgressClient.startRoomAudioEgress(egressToken, roomName, audioKey);
        meeting.setAudioEgressId(egressId);
        meetingRepository.save(meeting);
    }
}
