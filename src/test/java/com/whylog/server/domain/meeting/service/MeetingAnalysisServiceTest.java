package com.whylog.server.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.entity.DecisionTimeline;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.decision.repository.DecisionBaseRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.decision.repository.DecisionTimelineRepository;
import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.enums.MeetingRole;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.global.external.fast.client.FastApiTranscribeClient;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class MeetingAnalysisServiceTest {

    private MeetingUseCase meetingUseCase;
    private MeetingAudioReplayService meetingAudioReplayService;
    private MeetingAudioFileService meetingAudioFileService;
    private FastApiTranscribeClient fastApiTranscribeClient;
    private ApplicationRepository applicationRepository;
    private ApplicationBaseRepository applicationBaseRepository;
    private ApplicationTimelineRepository applicationTimelineRepository;
    private DecisionBaseRepository decisionBaseRepository;
    private DecisionTimelineRepository decisionTimelineRepository;
    private DecisionRepository decisionRepository;
    private MeetingAnalysisRepository meetingAnalysisRepository;
    private MeetingMemberRepository meetingMemberRepository;
    private MeetingLiveMessageBundleService meetingLiveMessageBundleService;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private MeetingAnalysisService meetingAnalysisService;

    @BeforeEach
    void setUp() {
        meetingUseCase = mock(MeetingUseCase.class);
        meetingAudioReplayService = mock(MeetingAudioReplayService.class);
        meetingAudioFileService = mock(MeetingAudioFileService.class);
        fastApiTranscribeClient = mock(FastApiTranscribeClient.class);
        applicationRepository = mock(ApplicationRepository.class);
        applicationBaseRepository = mock(ApplicationBaseRepository.class);
        applicationTimelineRepository = mock(ApplicationTimelineRepository.class);
        decisionBaseRepository = mock(DecisionBaseRepository.class);
        decisionTimelineRepository = mock(DecisionTimelineRepository.class);
        decisionRepository = mock(DecisionRepository.class);
        meetingAnalysisRepository = mock(MeetingAnalysisRepository.class);
        meetingMemberRepository = mock(MeetingMemberRepository.class);
        meetingLiveMessageBundleService = mock(MeetingLiveMessageBundleService.class);
        transactionManager = mock(PlatformTransactionManager.class);
        transactionTemplate = new TransactionTemplate(transactionManager);

        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        when(meetingAnalysisRepository.findByMeetingId(anyLong())).thenReturn(Optional.empty());
        when(meetingAnalysisRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisionRepository.findByMeetingId(anyLong())).thenReturn(Optional.empty());
        when(decisionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisionBaseRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationBaseRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(decisionTimelineRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applicationTimelineRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(meetingLiveMessageBundleService.buildLiveMessagesJson(any())).thenReturn(null);

        meetingAnalysisService = new MeetingAnalysisService(
                meetingUseCase,
                meetingAudioReplayService,
                meetingAudioFileService,
                fastApiTranscribeClient,
                applicationRepository,
                applicationBaseRepository,
                applicationTimelineRepository,
                decisionBaseRepository,
                decisionTimelineRepository,
                decisionRepository,
                meetingAnalysisRepository,
                meetingMemberRepository,
                meetingLiveMessageBundleService,
                transactionTemplate,
                new ObjectMapper()
        );
    }

    @Test
    void timelineMemberIdIsStoredDirectly() throws Exception {
        Meeting meeting = meetingWithMembers();
        when(meetingMemberRepository.existsByMemberIdAndMeetingId(1L, 10L)).thenReturn(true);
        when(meetingUseCase.findMeetingWithMembersById(10L)).thenReturn(meeting);

        TranscribeApplicationRunResponse response = response(
                timeline(100L, 2L, "핵심", "발화"),
                transcriptSegment(1L, 2L, "00:00:01", null, "안녕하세요"),
                transcriptSegment(2L, null, "00:00:02", null, "스킵"),
                transcriptSegment(3L, 999L, "00:00:03", null, "스킵2")
        );

        meetingAnalysisService.persistTestMeetingAnalysis(1L, 10L, MeetingRequest.MeetingAnalysisTestDTO.builder()
                .isSuccess(true)
                .code("OK")
                .message("ok")
                .result(response)
                .build());

        @SuppressWarnings("unchecked")
        var timelineCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(decisionTimelineRepository).saveAllAndFlush(timelineCaptor.capture());
        assertThat(timelineCaptor.getValue()).hasSize(1);
        assertThat(((DecisionTimeline) timelineCaptor.getValue().get(0)).getMemberId()).isEqualTo(2L);
    }

    @Test
    void segmentMemberIdIsUsedAndInvalidSegmentsAreSkipped() throws Exception {
        Meeting meeting = meetingWithMembers();
        when(meetingMemberRepository.existsByMemberIdAndMeetingId(1L, 10L)).thenReturn(true);
        when(meetingUseCase.findMeetingWithMembersById(10L)).thenReturn(meeting);

        TranscribeApplicationRunResponse response = response(
                timeline(100L, 2L, "핵심", "발화"),
                transcriptSegment(1L, 2L, "00:00:01", null, "memberId 우선"),
                transcriptSegment(2L, null, "00:00:02", null, "skip"),
                transcriptSegment(3L, 999L, "00:00:03", null, "skip2")
        );

        meetingAnalysisService.persistTestMeetingAnalysis(1L, 10L, MeetingRequest.MeetingAnalysisTestDTO.builder()
                .isSuccess(true)
                .code("OK")
                .message("ok")
                .result(response)
                .build());

        assertThat(meeting.getDialogues()).hasSize(1);
        assertThat(meeting.getDialogues().get(0).getMember().getId()).isEqualTo(2L);
    }

    private Meeting meetingWithMembers() {
        Meeting meeting = Meeting.create(
                MeetingRequest.MeetingCreateDTO.builder().name("회의").build(),
                mock(Team.class)
        );
        ReflectionTestUtils.setField(meeting, "id", 10L);
        ReflectionTestUtils.setField(meeting, "startDateTime", LocalDateTime.of(2026, 1, 1, 9, 0, 0));

        meeting.getMeetingMembers().add(MeetingMember.create(meeting, member(1L, "first"), MeetingRole.OWNER));
        meeting.getMeetingMembers().add(MeetingMember.create(meeting, member(2L, "second"), MeetingRole.GENERAL));
        return meeting;
    }

    private Member member(Long id, String name) {
        Member member = Member.builder()
                .name(name)
                .email(name + "@example.com")
                .password("pw")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private TranscribeApplicationRunResponse response(TranscribeApplicationRunResponse.ApplicationResponse applicationResponse,
                                                      TranscribeApplicationRunResponse.TranscriptSegmentResponse... segments) {
        return new TranscribeApplicationRunResponse(
                "run-1",
                "completed",
                "applications_ready",
                "10",
                null,
                null,
                null,
                null,
                null,
                new TranscribeApplicationRunResponse.TranscribeApplicationRunResult(
                        "10",
                        null,
                        List.of(segments),
                        new TranscribeApplicationRunResponse.AnalysisResultResponse(
                                new TranscribeApplicationRunResponse.OverallAnalysisResponse(
                                        new TranscribeApplicationRunResponse.MeetingInfoResponse("제목", "목적", "00:10:00"),
                                        List.of("토픽"),
                                        List.of("맥락"),
                                        List.of("적용사항"),
                                        List.of("사유")
                                ),
                                List.of(applicationResponse),
                                List.of()
                        )
                )
        );
    }

    private TranscribeApplicationRunResponse.ApplicationResponse timeline(Long applicationId,
                                                                          Long memberId,
                                                                          String content,
                                                                          String utterance) {
        return new TranscribeApplicationRunResponse.ApplicationResponse(
                applicationId,
                "적용사항",
                List.of("사유"),
                List.of(new TranscribeApplicationRunResponse.TimelineResponse(
                        "00:01:00",
                        "step",
                        memberId,
                        content,
                        utterance
                ))
        );
    }

    private TranscribeApplicationRunResponse.TranscriptSegmentResponse transcriptSegment(Long messageId,
                                                                                         Long memberId,
                                                                                         String startTime,
                                                                                         String endTime,
                                                                                         String text) {
        return new TranscribeApplicationRunResponse.TranscriptSegmentResponse(
                messageId,
                memberId,
                startTime,
                endTime,
                text,
                true
        );
    }
}
