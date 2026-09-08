package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Dialogue;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.exception.MeetingNotFoundException;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberDisplayResolver;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import com.whylog.server.global.external.s3.S3Client;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingQueryService {

    private final MeetingRepository meetingRepository;
    private final MeetingAudioReplayService meetingAudioReplayService;
    private final MemberDisplayResolver memberDisplayResolver;
    private final S3Client s3Client;

    // 미팅 목록 조회
    public List<MeetingResponse.MeetingListDTO> getMeetings(Long teamId, MeetingStatus status) {

        // 기본값: 진행완료
        MeetingStatus targetStatus = status != null ? status : MeetingStatus.COMPLETED;

        List<Meeting> meetings = meetingRepository.findWithAnalysis(teamId);

        return meetings.stream()
                .filter(m -> checkMeetingStatus(m, targetStatus)) // 상태 일치 체크
                .map(
                        m ->
                                MeetingResponse.MeetingListDTO.builder()
                                        .meetingId(m.getId())
                                        .name(m.getName())
                                        .status(m.getStatus())
                                        .elapse(
                                                !m.isOngoing()
                                                        ? m.getElapse()
                                                        : null) // 진행완료일 경우 null로 반환
                                        .build())
                .toList();
    }

    // 회의 기본 정보 조회
    public MeetingResponse.MeetingDetailDTO getMeetingDefaultInfo(Long meetingId) {

        if (meetingId == null) {
            throw new ParameterRequiredException();
        }

        Meeting meeting = findMeetingWithMembers(meetingId);
        List<Member> participants = participants(meeting);

        Map<Long, MemberDisplayResolver.DisplayMember> displayMembers =
                memberDisplayResolver.resolveByTeam(meeting.getTeam().getId(), participants);

        return MeetingResponse.MeetingDetailDTO.builder()
                .meetingId(meeting.getId())
                .name(meeting.getName())
                .startDateTime(meeting.getStartDateTime())
                .endDateTime(meeting.getEndDateTime())
                .duration(meeting.getDuration())
                .memberCount(participants.size())
                .members(memberToParticipantsInfo(participants, displayMembers))
                .audioDuration(meetingAudioReplayService.resolveAudioDurationIfAvailable(meeting))
                .build();
    }

    private List<MeetingResponse.MeetingParticipantInfo> memberToParticipantsInfo(
            List<Member> members, Map<Long, MemberDisplayResolver.DisplayMember> displayMembers) {
        return members.stream()
                .map(
                        member -> {
                            MemberDisplayResolver.DisplayMember displayMember =
                                    displayMember(member, displayMembers);
                            return MeetingResponse.MeetingParticipantInfo.builder()
                                    .memberId(displayMember.memberId())
                                    .name(displayMember.name())
                                    .profileImage(profileImageUrl(displayMember))
                                    .build();
                        })
                .toList();
    }

    private List<Member> participants(Meeting meeting) {
        return meeting.getMeetingMembers().stream().map(MeetingMember::getMember).toList();
    }

    private boolean checkMeetingStatus(Meeting meeting, MeetingStatus status) {
        return meeting.getStatus() == status;
    }

    public MeetingResponse.AudioDTO getMeetingAudio(Long meetingId) {
        Meeting meeting = findMeeting(meetingId);
        return meetingAudioReplayService.buildAudioResponse(meeting);
    }

    public MeetingResponse.AnalysisResultDTO getAnalysis(Long meetingId) {

        Meeting meeting = findMeetingWithAnalysis(meetingId);

        MeetingAnalysis meetingAnalysis = meeting.getMeetingAnalysis();

        if (meetingAnalysis == null) {
            return MeetingResponse.AnalysisResultDTO.createFalse(meetingId);
        }

        Integer audioDuration =
                meetingAudioReplayService.resolveAudioDurationIfAvailable(
                        meetingAnalysis.getMeeting());
        return MeetingResponse.AnalysisResultDTO.create(meetingAnalysis, audioDuration);
    }

    public MeetingResponse.HistoryListDTO getDialogueHistory(Long meetingId) {

        Meeting meeting = findMeetingWithDialogue(meetingId);
        Meeting meetingWithMembers = findMeetingWithMembers(meetingId);
        List<Dialogue> dialogues = meeting.getDialogues();
        List<Member> members =
                meetingWithMembers.getMeetingMembers().stream()
                        .map(MeetingMember::getMember)
                        .toList();
        Map<Long, MemberDisplayResolver.DisplayMember> displayMembers =
                memberDisplayResolver.resolveByTeam(meeting.getTeam().getId(), members);

        return createHistoryListDto(meeting, dialogues, members, displayMembers);
    }

    private MeetingResponse.HistoryListDTO createHistoryListDto(
            Meeting meeting,
            List<Dialogue> dialogues,
            List<Member> members,
            Map<Long, MemberDisplayResolver.DisplayMember> displayMembers) {
        return MeetingResponse.HistoryListDTO.builder()
                .participants(createParticipantDtos(members, displayMembers))
                .dialogues(createDialogueDtos(meeting, dialogues, displayMembers))
                .build();
    }

    private List<MeetingResponse.HistoryListDTO.ParticipantDTO> createParticipantDtos(
            List<Member> members, Map<Long, MemberDisplayResolver.DisplayMember> displayMembers) {
        return members.stream()
                .map(
                        member -> {
                            MemberDisplayResolver.DisplayMember displayMember =
                                    displayMember(member, displayMembers);
                            return MeetingResponse.HistoryListDTO.ParticipantDTO.builder()
                                    .memberId(displayMember.memberId())
                                    .name(displayMember.name())
                                    .profileImage(profileImageUrl(displayMember))
                                    .build();
                        })
                .toList();
    }

    private List<MeetingResponse.HistoryListDTO.DialogueDTO> createDialogueDtos(
            Meeting meeting,
            List<Dialogue> dialogues,
            Map<Long, MemberDisplayResolver.DisplayMember> displayMembers) {
        LocalDateTime startDateTime = meeting.getStartDateTime();

        return dialogues.stream()
                .map(
                        dialogue -> {
                            MemberDisplayResolver.DisplayMember displayMember =
                                    displayMember(dialogue.getMember(), displayMembers);
                            return MeetingResponse.HistoryListDTO.DialogueDTO.builder()
                                    .memberId(displayMember.memberId())
                                    .name(displayMember.name())
                                    .profileImage(profileImageUrl(displayMember))
                                    .content(dialogue.getContent())
                                    .timestamp(
                                            formatElapsed(
                                                    startDateTime, dialogue.getSpeechDateTime()))
                                    .build();
                        })
                .toList();
    }

    private MemberDisplayResolver.DisplayMember displayMember(
            Member member, Map<Long, MemberDisplayResolver.DisplayMember> displayMembers) {
        if (member == null || member.getId() == null) {
            return memberDisplayResolver.resolve(member, null);
        }
        return displayMembers.getOrDefault(
                member.getId(), memberDisplayResolver.resolve(member, null));
    }

    private String profileImageUrl(MemberDisplayResolver.DisplayMember displayMember) {
        return displayMember == null ? null : s3Client.getFileUrl(displayMember.profileImageKey());
    }

    private String formatElapsed(LocalDateTime startDateTime, LocalDateTime speechDateTime) {
        if (startDateTime == null || speechDateTime == null) {
            return null;
        }

        Duration elapsed = Duration.between(startDateTime, speechDateTime);
        if (elapsed.isNegative()) {
            return "00:00";
        }

        long totalSeconds = elapsed.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Meeting findMeeting(Long meetingId) {
        return meetingRepository.findById(meetingId).orElseThrow(MeetingNotFoundException::new);
    }

    private Meeting findMeetingWithMembers(Long meetingId) {
        return meetingRepository
                .findWithMembers(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
    }

    private Meeting findMeetingWithAnalysis(Long meetingId) {
        return meetingRepository
                .findByMeetingId(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
    }

    private Meeting findMeetingWithDialogue(Long meetingId) {
        return meetingRepository
                .findWithDialogue(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
    }
}
