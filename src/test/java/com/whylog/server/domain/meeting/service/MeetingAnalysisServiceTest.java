package com.whylog.server.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.decision.repository.DecisionBaseRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.decision.repository.DecisionTimelineRepository;
import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.repository.DialogueRepository;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.global.external.fast.client.FastApiTranscribeClient;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunCreateResponse;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

class MeetingAnalysisServiceTest {

    private FastApiTranscribeClient fastApiTranscribeClient;
    private MeetingAudioReplayService meetingAudioReplayService;
    private MeetingAudioFileService meetingAudioFileService;
    private MeetingUseCase meetingUseCase;
    private ApplicationRepository applicationRepository;
    private ApplicationBaseRepository applicationBaseRepository;
    private ApplicationTimelineRepository applicationTimelineRepository;
    private DecisionBaseRepository decisionBaseRepository;
    private DecisionTimelineRepository decisionTimelineRepository;
    private DecisionRepository decisionRepository;
    private MeetingAnalysisRepository meetingAnalysisRepository;
    private DialogueRepository dialogueRepository;
    private MeetingMemberRepository meetingMemberRepository;
    private MeetingLiveMessageBundleService meetingLiveMessageBundleService;
    private TransactionTemplate transactionTemplate;
    private MeetingAnalysisService meetingAnalysisService;

    @BeforeEach
    void setUp() {
        fastApiTranscribeClient = mock(FastApiTranscribeClient.class);
        meetingAudioReplayService = mock(MeetingAudioReplayService.class);
        meetingAudioFileService = mock(MeetingAudioFileService.class);
        meetingUseCase = mock(MeetingUseCase.class);
        applicationRepository = mock(ApplicationRepository.class);
        applicationBaseRepository = mock(ApplicationBaseRepository.class);
        applicationTimelineRepository = mock(ApplicationTimelineRepository.class);
        decisionBaseRepository = mock(DecisionBaseRepository.class);
        decisionTimelineRepository = mock(DecisionTimelineRepository.class);
        decisionRepository = mock(DecisionRepository.class);
        meetingAnalysisRepository = mock(MeetingAnalysisRepository.class);
        dialogueRepository = mock(DialogueRepository.class);
        meetingMemberRepository = mock(MeetingMemberRepository.class);
        meetingLiveMessageBundleService = mock(MeetingLiveMessageBundleService.class);
        transactionTemplate = mock(TransactionTemplate.class);

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
                dialogueRepository,
                meetingMemberRepository,
                meetingLiveMessageBundleService,
                transactionTemplate,
                new ObjectMapper()
        );
    }

    @Test
    void createTranscribeApplicationRunTransfersDrainedLiveMessages() throws Exception {
        Meeting meeting = meeting();

        when(meetingAudioFileService.extractFileName("audio-key")).thenReturn("audio.mp3");
        when(meetingAudioFileService.resolveResponseContentType("audio-key")).thenReturn("audio/mpeg");
        when(meetingLiveMessageBundleService.buildLiveMessagesJson(meeting)).thenReturn("[{\"type\":\"TEXT\",\"timestamp\":\"01:02:03\"}]");
        when(fastApiTranscribeClient.createTranscribeApplicationRun(
                any(Resource.class),
                eq("audio.mp3"),
                eq("audio/mpeg"),
                eq(null),
                eq("123"),
                eq(null),
                anyString()
        )).thenReturn(new FastApiResponse<>(
                true,
                "ok",
                "ok",
                new TranscribeApplicationRunCreateResponse("run-1", "queued", "queued", null, null)
        ));

        String runId = invokeCreateTranscribeApplicationRun(meeting);

        assertThat(runId).isEqualTo("run-1");

        org.mockito.ArgumentCaptor<String> liveMessagesCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(fastApiTranscribeClient).createTranscribeApplicationRun(
                any(Resource.class),
                eq("audio.mp3"),
                eq("audio/mpeg"),
                eq(null),
                eq("123"),
                eq(null),
                liveMessagesCaptor.capture()
        );

        String liveMessagesJson = liveMessagesCaptor.getValue();
        assertThat(liveMessagesJson).isEqualTo("[{\"type\":\"TEXT\",\"timestamp\":\"01:02:03\"}]");
    }

    @Test
    void createTranscribeApplicationRunPassesNullWhenNoLiveMessagesExist() throws Exception {
        Meeting meeting = meeting();

        when(meetingAudioFileService.extractFileName("audio-key")).thenReturn("audio.mp3");
        when(meetingAudioFileService.resolveResponseContentType("audio-key")).thenReturn("audio/mpeg");
        when(meetingLiveMessageBundleService.buildLiveMessagesJson(meeting)).thenReturn(null);
        when(fastApiTranscribeClient.createTranscribeApplicationRun(
                any(Resource.class),
                eq("audio.mp3"),
                eq("audio/mpeg"),
                eq(null),
                eq("123"),
                eq(null),
                eq(null)
        )).thenReturn(new FastApiResponse<>(
                true,
                "ok",
                "ok",
                new TranscribeApplicationRunCreateResponse("run-2", "queued", "queued", null, null)
        ));

        String runId = invokeCreateTranscribeApplicationRun(meeting);

        assertThat(runId).isEqualTo("run-2");
    }

    private String invokeCreateTranscribeApplicationRun(Meeting meeting) throws Exception {
        MeetingResponse.AudioDTO audioResponse = mock(MeetingResponse.AudioDTO.class);
        when(audioResponse.getAudioKey()).thenReturn("audio-key");
        when(audioResponse.getAudioUrl()).thenReturn("https://example.com/audio.mp3");

        Method method = MeetingAnalysisService.class.getDeclaredMethod(
                "createTranscribeApplicationRun",
                Meeting.class,
                MeetingResponse.AudioDTO.class
        );
        method.setAccessible(true);
        return (String) method.invoke(meetingAnalysisService, meeting, audioResponse);
    }

    private Meeting meeting() {
        Meeting meeting = Meeting.create(
                MeetingRequest.MeetingCreateDTO.builder().name("회의").build(),
                mock(Team.class)
        );
        ReflectionTestUtils.setField(meeting, "id", 123L);
        return meeting;
    }
}
