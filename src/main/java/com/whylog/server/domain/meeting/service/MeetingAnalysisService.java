package com.whylog.server.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.entity.DecisionBase;
import com.whylog.server.domain.decision.entity.DecisionTimeline;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.DecisionBaseRepository;
import com.whylog.server.domain.decision.repository.DecisionTimelineRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Dialogue;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.exception.MeetingInvalidMemberException;
import com.whylog.server.domain.meeting.exception.MeetingAudioNotReadyException;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.external.fast.client.FastApiTranscribeClient;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunCreateResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunResponse;
import com.whylog.server.global.external.fast.exception.FastApiErrorCode;
import com.whylog.server.global.external.fast.exception.FastApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingAnalysisService {

    private static final Duration AUDIO_READY_WAIT_INTERVAL = Duration.ofSeconds(3);
    private static final int MAX_AUDIO_READY_ATTEMPTS = 20;
    private static final Duration RUN_POLL_WAIT_INTERVAL = Duration.ofSeconds(3);
    private static final int MAX_RUN_POLL_ATTEMPTS = 120;

    private final MeetingUseCase meetingUseCase;
    private final MeetingAudioReplayService meetingAudioReplayService;
    private final MeetingAudioFileService meetingAudioFileService;
    private final FastApiTranscribeClient fastApiTranscribeClient;
    private final ApplicationRepository applicationRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final DecisionBaseRepository decisionBaseRepository;
    private final DecisionTimelineRepository decisionTimelineRepository;
    private final DecisionRepository decisionRepository;
    private final MeetingAnalysisRepository meetingAnalysisRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingLiveMessageBundleService meetingLiveMessageBundleService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    // 회의 종료 후 오디오 분석 전체 흐름을 시작한다. ( 외부 클래스에서 호출 )
    public void analyzeMeetingAudio(Long meetingId) {
        Meeting meeting = meetingUseCase.findMeetingWithMembersById(meetingId);
        MeetingResponse.AudioDTO audioResponse = resolveAudioWithRetry(meeting);

        String runId = createTranscribeApplicationRun(meeting, audioResponse);
        TranscribeApplicationRunResponse finalResponse = pollTranscribeApplicationRun(runId);
        persistMeetingAnalysis(meeting, finalResponse);
    }

    // FastAPI 응답 JSON을 받아 저장 로직을 테스트한다.
    public void persistTestMeetingAnalysis(Long memberId,
                                           Long meetingId,
                                           com.whylog.server.domain.meeting.dto.MeetingRequest.MeetingAnalysisTestDTO request) {
        if (!meetingMemberRepository.existsByMemberIdAndMeetingId(memberId, meetingId)) {
            throw new MeetingInvalidMemberException();
        }

        Meeting meeting = meetingUseCase.findMeetingWithMembersById(meetingId);
        TranscribeApplicationRunResponse response = request.getResult();
        if (response == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        persistMeetingAnalysis(meeting, response);
    }

    // 회의 오디오가 준비될 때까지 재시도하며 오디오 응답을 확보한다.
    private MeetingResponse.AudioDTO resolveAudioWithRetry(Meeting meeting) {
        MeetingAudioNotReadyException lastException = null;

        for (int attempt = 1; attempt <= MAX_AUDIO_READY_ATTEMPTS; attempt++) {
            try {
                return meetingAudioReplayService.buildAudioResponse(meeting);
            } catch (MeetingAudioNotReadyException e) {
                lastException = e;
                sleep(AUDIO_READY_WAIT_INTERVAL);
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new MeetingAudioNotReadyException();
    }

    // FastAPI 전사용 비동기 실행(run)을 생성하고 runId를 반환한다.
    private String createTranscribeApplicationRun(Meeting meeting, MeetingResponse.AudioDTO audioResponse) {
        String audioKey = audioResponse.getAudioKey();
        String audioUrl = audioResponse.getAudioUrl();
        String audioFilename = meetingAudioFileService.extractFileName(audioKey);
        String contentType = meetingAudioFileService.resolveResponseContentType(audioKey);
        String liveMessagesJson = meetingLiveMessageBundleService.buildLiveMessagesJson(meeting);

        FastApiResponse<TranscribeApplicationRunCreateResponse> createResponse = fastApiTranscribeClient.createTranscribeApplicationRun(
                buildAudioResource(audioUrl),
                audioFilename,
                contentType,
                null,
                String.valueOf(meeting.getId()),
                null,
                liveMessagesJson
        );

        String runId = requireResult(createResponse).runId();
        log.info("회의 오디오 분석 run 생성 완료: meetingId={}, runId={}", meeting.getId(), runId);
        return runId;
    }

    // FastAPI run 상태를 폴링하여 최종 완료 결과를 가져온다.
    private TranscribeApplicationRunResponse pollTranscribeApplicationRun(String runId) {
        FastApiException lastException = null;

        for (int attempt = 1; attempt <= MAX_RUN_POLL_ATTEMPTS; attempt++) {
            try {
                FastApiResponse<TranscribeApplicationRunResponse> response = fastApiTranscribeClient.getTranscribeApplicationRun(runId);
                TranscribeApplicationRunResponse runResponse = requireResult(response);

                if (isFailed(runResponse)) {
                    log.warn("FastAPI 전사 실행 실패: runId={}, error={}", runId, runResponse.error());
                    throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED);
                }

                if (isCompleted(runResponse)) {
                    return runResponse;
                }
            } catch (FastApiException e) {
                if (!isTransientRunNotFound(e)) {
                    throw e;
                }
                lastException = e;
            }

            sleep(RUN_POLL_WAIT_INTERVAL);
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED);
    }

    // run이 완료 상태인지 확인한다.
    private boolean isCompleted(TranscribeApplicationRunResponse response) {
        return "completed".equalsIgnoreCase(response.status())
                && "applications_ready".equalsIgnoreCase(response.phase());
    }

    // run이 실패 상태인지 확인한다.
    private boolean isFailed(TranscribeApplicationRunResponse response) {
        return "failed".equalsIgnoreCase(response.status()) || "failed".equalsIgnoreCase(response.phase());
    }

    // FastAPI run not found가 일시적 상태인지 판별한다.
    private boolean isTransientRunNotFound(FastApiException exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof HttpClientErrorException httpClientErrorException
                    && httpClientErrorException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return true;
            }
            if (cause instanceof RestClientResponseException restClientResponseException
                    && restClientResponseException.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    // FastAPI 분석 결과를 회의 도메인 엔티티와 대화 목록으로 저장한다.
    private void persistMeetingAnalysis(Meeting meeting, TranscribeApplicationRunResponse response) {
        TranscribeApplicationRunResponse.TranscribeApplicationRunResult runResult = response.result();
        if (runResult == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        List<TranscribeApplicationRunResponse.TranscriptSegmentResponse> transcriptSegments =
                Optional.ofNullable(runResult.transcriptSegments()).orElseGet(List::of);
        TranscribeApplicationRunResponse.AnalysisResultResponse analysisResult = runResult.analysisResult();
        TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis =
                analysisResult != null ? analysisResult.overallAnalysis() : null;
        List<TranscribeApplicationRunResponse.ApplicationResponse> applications =
                analysisResult != null && analysisResult.applications() != null ? analysisResult.applications() : List.of();
        MeetingAnalysis.MeetingAnalysisPayload payload = buildMeetingAnalysisPayload(overallAnalysis);

        transactionTemplate.executeWithoutResult(status -> {
            Meeting managedMeeting = meetingUseCase.findMeetingWithMembersById(meeting.getId());
            MeetingAnalysis meetingAnalysis = meetingAnalysisRepository.findByMeetingId(managedMeeting.getId())
                    .map(existingMeetingAnalysis -> {
                        existingMeetingAnalysis.updateAnalysis(payload);
                        return existingMeetingAnalysis;
                    })
                    .orElseGet(() -> MeetingAnalysis.create(managedMeeting, payload));
            meetingAnalysisRepository.save(meetingAnalysis);
            managedMeeting.attachMeetingAnalysis(meetingAnalysis);

            List<Dialogue> dialogues = buildDialogues(managedMeeting, transcriptSegments);
            managedMeeting.getDialogues().clear();
            managedMeeting.getDialogues().addAll(dialogues);

            Decision decision = createDecisionIfAbsent(managedMeeting);
            replaceApplications(managedMeeting.getId(), decision, applications);
        });

        log.info("회의 오디오 분석 저장 완료: meetingId={}, transcriptSegmentCount={}", meeting.getId(), transcriptSegments.size());
        // TODO: applications 저장 후 applicationId를 발급해 /api/meeting-analysis/embeddings로 전달한다.
    }

    // Decision이 없을 때 새로 생성한다.
    private Decision createDecisionIfAbsent(Meeting meeting) {
        return decisionRepository.findByMeetingId(meeting.getId())
                .orElseGet(() -> {
                    Decision decision = decisionRepository.save(Decision.create(meeting, true));
                    log.info("결정사항 저장 완료: meetingId={}, decisionId={}", meeting.getId(), decision.getId());
                    return decision;
                });
    }

    // 분석 결과의 적용사항 제목 목록을 저장한다.
    private void replaceApplications(Long meetingId,
                                     Decision decision,
                                     List<TranscribeApplicationRunResponse.ApplicationResponse> applications) {
        applicationBaseRepository.deleteByMeetingId(meetingId);
        applicationTimelineRepository.deleteByMeetingId(meetingId);
        decisionBaseRepository.deleteByMeetingId(meetingId);
        decisionTimelineRepository.deleteByMeetingId(meetingId);
        applicationRepository.deleteByMeetingId(meetingId);

        List<TranscribeApplicationRunResponse.ApplicationResponse> validApplications = applications.stream()
                .filter(application -> application != null
                        && application.applicationTitle() != null
                        && !application.applicationTitle().isBlank())
                .toList();

        List<Application> newApplications = validApplications.stream()
                .map(application -> Application.create(
                        decision,
                        application.applicationTitle().trim()
                ))
                .toList();

        if (!newApplications.isEmpty()) {
            List<Application> savedApplications = applicationRepository.saveAllAndFlush(newApplications);
            persistApplicationDetails(savedApplications, validApplications);
            log.info("적용사항 저장 완료: meetingId={}, decisionId={}, applicationCount={}",
                    meetingId, decision.getId(), newApplications.size());
        }
    }

    // 저장된 적용사항 엔티티에 reason/timeline 세부 정보를 순서대로 연결 저장한다.
    private void persistApplicationDetails(List<Application> applications,
                                           List<TranscribeApplicationRunResponse.ApplicationResponse> applicationResponses) {
        for (int index = 0; index < applications.size(); index++) {
            Application application = applications.get(index);
            TranscribeApplicationRunResponse.ApplicationResponse response = applicationResponses.get(index);
            persistApplicationReasons(application, response.applicationReasons());
            persistApplicationTimelines(application, response.timeline());
        }
    }

    // 적용사항 reason 목록을 DecisionBase/ApplicationBase로 분리 저장한다.
    private void persistApplicationReasons(Application application, List<String> reasons) {
        List<String> validReasons = safeStrings(reasons);
        if (validReasons.isEmpty()) {
            return;
        }

        List<DecisionBase> decisionBases = validReasons.stream()
                .map(reason -> DecisionBase.create(application.getDecision(), reason.trim()))
                .toList();
        List<DecisionBase> savedDecisionBases = decisionBaseRepository.saveAllAndFlush(decisionBases);

        List<ApplicationBase> applicationBases = savedDecisionBases.stream()
                .map(decisionBase -> ApplicationBase.create(application, decisionBase))
                .toList();
        applicationBaseRepository.saveAllAndFlush(applicationBases);
    }

    // 적용사항 timeline 목록을 DecisionTimeline/ApplicationTimeline으로 분리 저장한다.
    private void persistApplicationTimelines(Application application,
                                             List<TranscribeApplicationRunResponse.TimelineResponse> timelines) {
        if (timelines == null || timelines.isEmpty()) {
            return;
        }

        List<DecisionTimeline> decisionTimelines = timelines.stream()
                .filter(timeline -> timeline != null)
                .map(timeline -> DecisionTimeline.create(
                        application.getDecision(),
                        timeline.timestamp(),
                        timeline.step(),
                        timeline.content(),
                        timeline.memberId(),
                        timeline.utterance()
                ))
                .toList();

        if (decisionTimelines.isEmpty()) {
            return;
        }

        List<DecisionTimeline> savedDecisionTimelines = decisionTimelineRepository.saveAllAndFlush(decisionTimelines);
        List<ApplicationTimeline> applicationTimelines = savedDecisionTimelines.stream()
                .map(decisionTimeline -> ApplicationTimeline.create(application, decisionTimeline))
                .toList();
        applicationTimelineRepository.saveAllAndFlush(applicationTimelines);
    }

    // null 이거나 비어 있는 문자열을 제외한 값만 반환한다.
    private List<String> safeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }


    // OverallAnalysis를 MeetingAnalysis 저장용 payload로 변환한다.
    private MeetingAnalysis.MeetingAnalysisPayload buildMeetingAnalysisPayload(
            TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis
    ) {
        if (overallAnalysis == null) {
            return new MeetingAnalysis.MeetingAnalysisPayload(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        String analysisContent = serializeOverallAnalysis(overallAnalysis);
        String meetingTitle = overallAnalysis.meetingInfo() != null ? overallAnalysis.meetingInfo().title() : null;
        String meetingPurpose = overallAnalysis.meetingInfo() != null ? overallAnalysis.meetingInfo().purpose() : null;
        String meetingDuration = overallAnalysis.meetingInfo() != null ? overallAnalysis.meetingInfo().duration() : null;
        List<String> topics = overallAnalysis.topics();
        List<String> coreContext = overallAnalysis.coreContext();
        List<String> applicationTitles = overallAnalysis.applicationTitles();
        List<String> applicationReasons = overallAnalysis.applicationReasons();

        return new MeetingAnalysis.MeetingAnalysisPayload(
                meetingTitle,
                meetingPurpose,
                meetingDuration,
                analysisContent,
                topics,
                coreContext,
                applicationTitles,
                applicationReasons
        );
    }

    // OverallAnalysis를 원본 JSON 문자열로 직렬화한다.
    private String serializeOverallAnalysis(TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis) {
        if (overallAnalysis == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(overallAnalysis);
        } catch (JsonProcessingException e) {
            log.warn("회의 분석 결과 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }

    // 전사 세그먼트를 회의 참여자와 매칭해 Dialogue 목록으로 변환한다.
    private List<Dialogue> buildDialogues(Meeting meeting,
                                          List<TranscribeApplicationRunResponse.TranscriptSegmentResponse> transcriptSegments) {
        if (transcriptSegments == null || transcriptSegments.isEmpty()) {
            return List.of();
        }

        Map<Long, Member> membersById = meeting.getMeetingMembers().stream()
                .map(MeetingMember::getMember)
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        if (membersById.isEmpty()) {
            return List.of();
        }

        List<Dialogue> dialogues = new ArrayList<>();
        for (int index = 0; index < transcriptSegments.size(); index++) {
            TranscribeApplicationRunResponse.TranscriptSegmentResponse segment = transcriptSegments.get(index);
            if (segment == null || segment.text() == null || segment.text().isBlank()) {
                continue;
            }

            Member member = segment.memberId() != null ? membersById.get(segment.memberId()) : null;
            if (member == null) {
                continue;
            }

            LocalDateTime speechDateTime = resolveSpeechDateTime(meeting.getStartDateTime(), segment.startTime(), index);
            dialogues.add(Dialogue.create(meeting, member, segment.text().trim(), speechDateTime));
        }

        return dialogues;
    }

    // 전사 시작 시각 offset을 회의 시작 시각 기준 LocalDateTime으로 변환한다.
    private LocalDateTime resolveSpeechDateTime(LocalDateTime meetingStartDateTime, String offset, int fallbackIndex) {
        if (meetingStartDateTime == null) {
            return LocalDateTime.now();
        }

        Duration duration = parseDuration(offset);
        if (duration != null) {
            return meetingStartDateTime.plus(duration);
        }

        return meetingStartDateTime.plusSeconds(fallbackIndex);
    }

    // 시간 문자열을 Duration으로 변환한다.
    private Duration parseDuration(String offset) {
        if (offset == null || offset.isBlank()) {
            return null;
        }

        String[] parts = offset.trim().split(":");
        if (parts.length != 3) {
            return null;
        }

        try {
            long hours = Long.parseLong(parts[0]);
            long minutes = Long.parseLong(parts[1]);
            double seconds = Double.parseDouble(parts[2]);
            return Duration.ofHours(hours)
                    .plusMinutes(minutes)
                    .plusMillis(Math.round(seconds * 1000));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // FastAPI 응답의 result가 비어 있으면 예외를 던진다.
    private <T> T requireResult(FastApiResponse<T> response) {
        if (response == null || response.result() == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }
        return response.result();
    }

    // FastAPI에 전달할 오디오 URL Resource를 만든다.
    private UrlResource buildAudioResource(String audioUrl) {
        try {
            return new UrlResource(audioUrl);
        } catch (Exception e) {
            throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED, e);
        }
    }

    // 폴링/재시도 사이에 잠시 대기한다.
    private void sleep(Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED, e);
        }
    }
}
