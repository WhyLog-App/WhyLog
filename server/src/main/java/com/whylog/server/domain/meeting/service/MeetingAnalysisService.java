package com.whylog.server.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.ApplicationBase;
import com.whylog.server.domain.decision.entity.ApplicationTimeline;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.entity.DecisionBase;
import com.whylog.server.domain.decision.entity.DecisionTimeline;
import com.whylog.server.domain.decision.repository.ApplicationBaseRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.ApplicationTimelineRepository;
import com.whylog.server.domain.decision.repository.DecisionBaseRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.decision.repository.DecisionTimelineRepository;
import com.whylog.server.domain.decision.service.DecisionCommitMatchService;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Dialogue;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.entity.MeetingAnalysis;
import com.whylog.server.domain.meeting.entity.MeetingMember;
import com.whylog.server.domain.meeting.exception.MeetingInvalidMemberException;
import com.whylog.server.domain.meeting.repository.DialogueRepository;
import com.whylog.server.domain.meeting.repository.MeetingAnalysisRepository;
import com.whylog.server.domain.meeting.repository.MeetingMemberRepository;
import com.whylog.server.domain.meeting.socket.util.WebSocketTimeUtil;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import com.whylog.server.global.apiPayload.exception.GeneralException;
import com.whylog.server.global.external.fast.client.FastApiMeetingAnalysisClient;
import com.whylog.server.global.external.fast.client.FastApiTranscribeClient;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.ApplicationEmbeddingsRequest;
import com.whylog.server.global.external.fast.dto.request.MeetingAnalysisRequest;
import com.whylog.server.global.external.fast.dto.request.TranscriptSegmentPayload;
import com.whylog.server.global.external.fast.dto.response.ApplicationEmbeddingsResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunCreateResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunResponse;
import com.whylog.server.global.external.fast.exception.FastApiErrorCode;
import com.whylog.server.global.external.fast.exception.FastApiException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    private static final Duration RUN_POLL_WAIT_INTERVAL = Duration.ofSeconds(3);
    private static final int MAX_RUN_POLL_ATTEMPTS = 120;

    private final MeetingUseCase meetingUseCase;
    private final MeetingAudioReplayService meetingAudioReplayService;
    private final MeetingAudioKeyResolver meetingAudioKeyResolver;
    private final DialogueRepository dialogueRepository;
    private final MeetingAudioFileService meetingAudioFileService;
    private final FastApiTranscribeClient fastApiTranscribeClient;
    private final FastApiMeetingAnalysisClient fastApiMeetingAnalysisClient;
    private final ApplicationRepository applicationRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final ApplicationTimelineRepository applicationTimelineRepository;
    private final DecisionBaseRepository decisionBaseRepository;
    private final DecisionTimelineRepository decisionTimelineRepository;
    private final DecisionRepository decisionRepository;
    private final MeetingAnalysisRepository meetingAnalysisRepository;
    private final MeetingMemberRepository meetingMemberRepository;
    private final MeetingLiveMessageBundleService meetingLiveMessageBundleService;
    private final DecisionCommitMatchService decisionCommitMatchService;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 회의 종료 후 분석 전체 흐름을 시작한다. ( 외부 클래스에서 호출 )
     *
     * <p>실시간 회의가 WebRTC로 바뀌면서 녹음이 사라졌다. 재생 가능한 오디오가 있는 회의(과거 데이터)는 기존 전사 경로를 그대로 타고, 없으면 실시간 자막만으로
     * 분석한다. audioKey는 회의 생성 시점에 항상 채워지므로 분기 기준이 될 수 없어, S3에 실제 파일이 있는지로 판단한다.
     */
    public void analyzeMeetingAudio(Long meetingId) {
        Meeting meeting = meetingUseCase.findMeetingWithMembersById(meetingId);

        if (meetingAudioKeyResolver.resolvePlayableAudioKey(meeting).isPresent()) {
            analyzeWithAudio(meeting);
            return;
        }
        analyzeWithTranscript(meeting);
    }

    // 오디오가 있는 회의를 전사부터 돌린다.
    private void analyzeWithAudio(Meeting meeting) {
        MeetingResponse.AudioDTO audioResponse =
                meetingAudioReplayService.buildAudioResponse(meeting);

        String runId = createTranscribeApplicationRun(meeting, audioResponse);
        TranscribeApplicationRunResponse finalResponse = pollTranscribeApplicationRun(runId);
        persistMeetingAnalysis(meeting, finalResponse);
    }

    /** 저장된 실시간 자막만으로 회의를 분석한다. 자막은 이미 Dialogue로 저장돼 있으므로 분석 결과로 덮어쓰지 않는다. */
    private void analyzeWithTranscript(Meeting meeting) {
        List<Dialogue> dialogues =
                dialogueRepository.findAllByMeetingIdOrderBySpeechTime(meeting.getId());
        if (dialogues.isEmpty()) {
            log.warn("실시간 자막이 없어 회의 분석을 건너뛴다: meetingId={}", meeting.getId());
            return;
        }

        List<TranscriptSegmentPayload> segments = buildTranscriptSegments(meeting, dialogues);
        MeetingAnalysisRequest request =
                new MeetingAnalysisRequest(
                        String.valueOf(meeting.getId()), null, objectMapper.valueToTree(segments));

        FastApiResponse<JsonNode> response =
                fastApiMeetingAnalysisClient.extractMeetingAnalysis(request);
        TranscribeApplicationRunResponse.AnalysisResultResponse analysisResult =
                readAnalysisResult(requireResult(response));

        log.info("자막 기반 회의 분석 완료: meetingId={}, segmentCount={}", meeting.getId(), segments.size());
        persistAnalysis(meeting, analysisResult, List.of(), false);
    }

    /**
     * Dialogue를 FastAPI 전사 세그먼트 형식으로 되돌린다. speaker와 isFinal은 FastAPI 필수 필드라 비우면 422가 난다. 발화 종료 시각은
     * 저장하지 않으므로 다음 발화의 시작 시각으로 대신한다.
     */
    private List<TranscriptSegmentPayload> buildTranscriptSegments(
            Meeting meeting, List<Dialogue> dialogues) {
        LocalDateTime meetingStart = meeting.getStartDateTime();
        List<String> startTimes =
                dialogues.stream()
                        .map(
                                dialogue ->
                                        WebSocketTimeUtil.formatElapsed(
                                                meetingStart, dialogue.getSpeechDateTime()))
                        .toList();

        List<TranscriptSegmentPayload> segments = new ArrayList<>();
        for (int index = 0; index < dialogues.size(); index++) {
            Dialogue dialogue = dialogues.get(index);
            String startTime = startTimes.get(index);
            String endTime = index + 1 < startTimes.size() ? startTimes.get(index + 1) : startTime;
            segments.add(
                    new TranscriptSegmentPayload(
                            (long) (index + 1),
                            dialogue.getMember().getName(),
                            dialogue.getMember().getId(),
                            startTime,
                            endTime,
                            dialogue.getContent(),
                            true));
        }
        return segments;
    }

    // 재추출 응답에서 분석 결과만 꺼낸다. 전사 세그먼트는 응답에 포함되지 않는다.
    private TranscribeApplicationRunResponse.AnalysisResultResponse readAnalysisResult(
            JsonNode result) {
        JsonNode analysisResult = result.get("analysis_result");
        if (analysisResult == null || analysisResult.isNull()) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }
        return objectMapper.convertValue(
                analysisResult, TranscribeApplicationRunResponse.AnalysisResultResponse.class);
    }

    // FastAPI 응답 JSON을 받아 저장 로직을 테스트한다.
    public void persistTestMeetingAnalysis(
            Long memberId,
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

    // FastAPI 전사용 비동기 실행(run)을 생성하고 runId를 반환한다.
    private String createTranscribeApplicationRun(
            Meeting meeting, MeetingResponse.AudioDTO audioResponse) {
        String audioKey = audioResponse.getAudioKey();
        String audioUrl = audioResponse.getAudioUrl();
        String audioFilename = meetingAudioFileService.extractFileName(audioKey);
        String contentType = meetingAudioFileService.resolveResponseContentType(audioKey);
        String liveMessagesJson = meetingLiveMessageBundleService.buildLiveMessagesJson(meeting);

        FastApiResponse<TranscribeApplicationRunCreateResponse> createResponse =
                fastApiTranscribeClient.createTranscribeApplicationRun(
                        buildAudioResource(audioUrl),
                        audioFilename,
                        contentType,
                        null,
                        String.valueOf(meeting.getId()),
                        null,
                        liveMessagesJson);

        String runId = requireResult(createResponse).runId();
        log.info("회의 오디오 분석 run 생성 완료: meetingId={}, runId={}", meeting.getId(), runId);
        return runId;
    }

    // FastAPI run 상태를 폴링하여 최종 완료 결과를 가져온다.
    private TranscribeApplicationRunResponse pollTranscribeApplicationRun(String runId) {
        FastApiException lastException = null;

        for (int attempt = 1; attempt <= MAX_RUN_POLL_ATTEMPTS; attempt++) {
            try {
                FastApiResponse<TranscribeApplicationRunResponse> response =
                        fastApiTranscribeClient.getTranscribeApplicationRun(runId);
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
        return "failed".equalsIgnoreCase(response.status())
                || "failed".equalsIgnoreCase(response.phase());
    }

    // FastAPI run not found가 일시적 상태인지 판별한다.
    private boolean isTransientRunNotFound(FastApiException exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof HttpClientErrorException httpClientErrorException
                    && httpClientErrorException.getStatusCode().value()
                            == HttpStatus.NOT_FOUND.value()) {
                return true;
            }
            if (cause instanceof RestClientResponseException restClientResponseException
                    && restClientResponseException.getStatusCode().value()
                            == HttpStatus.NOT_FOUND.value()) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    // FastAPI 분석 결과를 회의 도메인 엔티티와 대화 목록으로 저장한다.
    private void persistMeetingAnalysis(
            Meeting meeting, TranscribeApplicationRunResponse response) {
        TranscribeApplicationRunResponse.TranscribeApplicationRunResult runResult =
                response.result();
        if (runResult == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        List<TranscribeApplicationRunResponse.TranscriptSegmentResponse> transcriptSegments =
                Optional.ofNullable(runResult.transcriptSegments()).orElseGet(List::of);
        persistAnalysis(meeting, runResult.analysisResult(), transcriptSegments, true);
    }

    /**
     * 분석 결과를 회의 도메인 엔티티로 저장한다.
     *
     * <p>{@code replaceDialogues}가 true면 회의록을 전사 세그먼트로 통째로 교체한다. 자막 기반 경로는 이미 실시간으로 저장해 둔 Dialogue가
     * 원본이므로 false로 넘겨야 한다. true로 넘기면서 세그먼트가 비어 있으면 orphanRemoval로 기존 회의록이 전부 삭제된다.
     */
    private void persistAnalysis(
            Meeting meeting,
            TranscribeApplicationRunResponse.AnalysisResultResponse analysisResult,
            List<TranscribeApplicationRunResponse.TranscriptSegmentResponse> transcriptSegments,
            boolean replaceDialogues) {
        TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis =
                analysisResult != null ? analysisResult.overallAnalysis() : null;
        List<TranscribeApplicationRunResponse.ApplicationResponse> applications =
                analysisResult != null && analysisResult.applications() != null
                        ? analysisResult.applications()
                        : List.of();
        MeetingAnalysis.MeetingAnalysisPayload payload =
                buildMeetingAnalysisPayload(overallAnalysis);

        SavedApplications savedApplications =
                transactionTemplate.execute(
                        status -> {
                            Meeting managedMeeting =
                                    meetingUseCase.findMeetingWithMembersById(meeting.getId());
                            MeetingAnalysis meetingAnalysis =
                                    meetingAnalysisRepository
                                            .findByMeetingId(managedMeeting.getId())
                                            .map(
                                                    existingMeetingAnalysis -> {
                                                        existingMeetingAnalysis.updateAnalysis(
                                                                payload);
                                                        return existingMeetingAnalysis;
                                                    })
                                            .orElseGet(
                                                    () ->
                                                            MeetingAnalysis.create(
                                                                    managedMeeting, payload));
                            meetingAnalysisRepository.save(meetingAnalysis);
                            managedMeeting.attachMeetingAnalysis(meetingAnalysis);

                            if (replaceDialogues) {
                                List<Dialogue> dialogues =
                                        buildDialogues(managedMeeting, transcriptSegments);
                                managedMeeting.getDialogues().clear();
                                managedMeeting.getDialogues().addAll(dialogues);
                            }

                            Decision decision = createDecisionIfAbsent(managedMeeting);
                            return replaceApplications(
                                    managedMeeting.getId(), decision, applications);
                        });
        if (savedApplications == null) {
            savedApplications = SavedApplications.empty(null);
        }

        log.info(
                "회의 분석 저장 완료: meetingId={}, transcriptSegmentCount={}, replaceDialogues={}",
                meeting.getId(),
                transcriptSegments.size(),
                replaceDialogues);
        sendApplicationEmbeddingsSafely(meeting, analysisResult, savedApplications);
        matchApplicationCommitsSafely(meeting, savedApplications);
    }

    // Decision이 없을 때 새로 생성한다.
    private Decision createDecisionIfAbsent(Meeting meeting) {
        return decisionRepository
                .findByMeetingId(meeting.getId())
                .orElseGet(
                        () -> {
                            Decision decision =
                                    decisionRepository.save(Decision.create(meeting, true));
                            log.info(
                                    "결정사항 저장 완료: meetingId={}, decisionId={}",
                                    meeting.getId(),
                                    decision.getId());
                            return decision;
                        });
    }

    // 분석 결과의 적용사항 제목 목록을 저장한다.
    private SavedApplications replaceApplications(
            Long meetingId,
            Decision decision,
            List<TranscribeApplicationRunResponse.ApplicationResponse> applications) {
        applicationBaseRepository.deleteByMeetingId(meetingId);
        applicationTimelineRepository.deleteByMeetingId(meetingId);
        decisionBaseRepository.deleteByMeetingId(meetingId);
        decisionTimelineRepository.deleteByMeetingId(meetingId);
        applicationRepository.deleteByMeetingId(meetingId);

        List<TranscribeApplicationRunResponse.ApplicationResponse> validApplications =
                applications.stream()
                        .filter(
                                application ->
                                        application != null
                                                && application.applicationTitle() != null
                                                && !application.applicationTitle().isBlank())
                        .toList();

        List<Application> newApplications =
                validApplications.stream()
                        .map(
                                application ->
                                        Application.create(
                                                decision, application.applicationTitle().trim()))
                        .toList();

        if (!newApplications.isEmpty()) {
            List<Application> savedApplications =
                    applicationRepository.saveAllAndFlush(newApplications);
            persistApplicationDetails(savedApplications, validApplications);
            log.info(
                    "적용사항 저장 완료: meetingId={}, decisionId={}, applicationCount={}",
                    meetingId,
                    decision.getId(),
                    newApplications.size());
            return new SavedApplications(decision.getId(), savedApplications, validApplications);
        }

        return SavedApplications.empty(decision.getId());
    }

    // 저장된 적용사항 엔티티에 reason/timeline 세부 정보를 순서대로 연결 저장한다.
    private void persistApplicationDetails(
            List<Application> applications,
            List<TranscribeApplicationRunResponse.ApplicationResponse> applicationResponses) {
        for (int index = 0; index < applications.size(); index++) {
            Application application = applications.get(index);
            TranscribeApplicationRunResponse.ApplicationResponse response =
                    applicationResponses.get(index);
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

        List<DecisionBase> decisionBases =
                validReasons.stream()
                        .map(
                                reason ->
                                        DecisionBase.create(
                                                application.getDecision(), reason.trim()))
                        .toList();
        List<DecisionBase> savedDecisionBases =
                decisionBaseRepository.saveAllAndFlush(decisionBases);

        List<ApplicationBase> applicationBases =
                savedDecisionBases.stream()
                        .map(decisionBase -> ApplicationBase.create(application, decisionBase))
                        .toList();
        applicationBaseRepository.saveAllAndFlush(applicationBases);
    }

    // 적용사항 timeline 목록을 DecisionTimeline/ApplicationTimeline으로 분리 저장한다.
    private void persistApplicationTimelines(
            Application application,
            List<TranscribeApplicationRunResponse.TimelineResponse> timelines) {
        if (timelines == null || timelines.isEmpty()) {
            return;
        }

        List<DecisionTimeline> decisionTimelines =
                timelines.stream()
                        .filter(timeline -> timeline != null)
                        .map(
                                timeline ->
                                        DecisionTimeline.create(
                                                application.getDecision(),
                                                timeline.timestamp(),
                                                timeline.step(),
                                                timeline.content(),
                                                timeline.memberId(),
                                                timeline.utterance()))
                        .toList();

        if (decisionTimelines.isEmpty()) {
            return;
        }

        List<DecisionTimeline> savedDecisionTimelines =
                decisionTimelineRepository.saveAllAndFlush(decisionTimelines);
        List<ApplicationTimeline> applicationTimelines =
                savedDecisionTimelines.stream()
                        .map(
                                decisionTimeline ->
                                        ApplicationTimeline.create(application, decisionTimeline))
                        .toList();
        applicationTimelineRepository.saveAllAndFlush(applicationTimelines);
    }

    // 저장된 적용사항을 FastAPI 임베딩 요청으로 전달한다.
    private void sendApplicationEmbeddingsSafely(
            Meeting meeting,
            TranscribeApplicationRunResponse.AnalysisResultResponse analysisResult,
            SavedApplications savedApplications) {
        if (savedApplications.savedApplications().isEmpty()) {
            log.info("저장된 적용사항이 없어 임베딩 호출을 생략한다: meetingId={}", meeting.getId());
            return;
        }

        try {
            ApplicationEmbeddingsRequest request =
                    buildEmbeddingsRequest(meeting, analysisResult, savedApplications);
            FastApiResponse<ApplicationEmbeddingsResponse> response =
                    fastApiMeetingAnalysisClient.createApplicationEmbeddings(request);
            Integer totalDocuments =
                    response != null && response.result() != null
                            ? response.result().totalDocuments()
                            : null;
            log.info(
                    "적용사항 임베딩 저장 완료: meetingId={}, totalDocuments={}",
                    meeting.getId(),
                    totalDocuments);
        } catch (Exception exception) {
            log.error("적용사항 임베딩 호출 실패: meetingId={}", meeting.getId(), exception);
        }
    }

    // 회의 분석 저장 후 적용사항-커밋 추천 매칭을 자동 실행한다.
    private void matchApplicationCommitsSafely(
            Meeting meeting, SavedApplications savedApplications) {
        if (savedApplications.decisionId() == null
                || savedApplications.savedApplications().isEmpty()) {
            log.info("저장된 적용사항이 없어 커밋 추천 매칭을 생략한다: meetingId={}", meeting.getId());
            return;
        }

        try {
            decisionCommitMatchService.matchApplicationCommits(savedApplications.decisionId());
            log.info(
                    "적용사항-커밋 추천 매칭 완료: meetingId={}, decisionId={}",
                    meeting.getId(),
                    savedApplications.decisionId());
        } catch (GeneralException exception) {
            ErrorReasonDTO reason = exception.getErrorReason();
            log.error(
                    "적용사항-커밋 추천 매칭 실패: meetingId={}, decisionId={}, errorCode={}, message={}",
                    meeting.getId(),
                    savedApplications.decisionId(),
                    reason.getCode(),
                    reason.getMessage(),
                    exception);
        } catch (Exception exception) {
            log.error(
                    "적용사항-커밋 추천 매칭 실패: meetingId={}, decisionId={}",
                    meeting.getId(),
                    savedApplications.decisionId(),
                    exception);
        }
    }

    // FastAPI 임베딩 요청을 생성한다.
    private ApplicationEmbeddingsRequest buildEmbeddingsRequest(
            Meeting meeting,
            TranscribeApplicationRunResponse.AnalysisResultResponse analysisResult,
            SavedApplications savedApplications) {
        TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis =
                analysisResult != null ? analysisResult.overallAnalysis() : null;
        List<String> otherMentions =
                analysisResult != null && analysisResult.otherMentions() != null
                        ? analysisResult.otherMentions()
                        : List.of();

        List<ApplicationEmbeddingsRequest.ApplicationPayload> applicationPayloads =
                new ArrayList<>();
        List<Application> applications = savedApplications.savedApplications();
        List<TranscribeApplicationRunResponse.ApplicationResponse> sourceApplications =
                savedApplications.sourceApplications();
        for (int index = 0; index < applications.size(); index++) {
            Application savedApplication = applications.get(index);
            TranscribeApplicationRunResponse.ApplicationResponse sourceApplication =
                    sourceApplications.get(index);
            applicationPayloads.add(
                    new ApplicationEmbeddingsRequest.ApplicationPayload(
                            savedApplication.getId(),
                            sourceApplication.applicationTitle(),
                            sourceApplication.applicationReasons(),
                            mapTimelinePayloads(sourceApplication.timeline())));
        }

        return new ApplicationEmbeddingsRequest(
                String.valueOf(meeting.getId()),
                null,
                new ApplicationEmbeddingsRequest.AnalysisResultPayload(
                        overallAnalysis, applicationPayloads, otherMentions));
    }

    // FastAPI 응답 타임라인을 임베딩 요청 payload로 변환한다.
    private List<ApplicationEmbeddingsRequest.TimelinePayload> mapTimelinePayloads(
            List<TranscribeApplicationRunResponse.TimelineResponse> timelines) {
        if (timelines == null || timelines.isEmpty()) {
            return List.of();
        }

        return timelines.stream()
                .filter(timeline -> timeline != null)
                .map(
                        timeline ->
                                new ApplicationEmbeddingsRequest.TimelinePayload(
                                        timeline.timestamp(),
                                        timeline.step(),
                                        timeline.memberId(),
                                        timeline.content(),
                                        timeline.utterance()))
                .toList();
    }

    // null 이거나 비어 있는 문자열을 제외한 값만 반환한다.
    private List<String> safeStrings(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream().filter(value -> value != null && !value.isBlank()).toList();
    }

    // OverallAnalysis를 MeetingAnalysis 저장용 payload로 변환한다.
    private MeetingAnalysis.MeetingAnalysisPayload buildMeetingAnalysisPayload(
            TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis) {
        if (overallAnalysis == null) {
            return new MeetingAnalysis.MeetingAnalysisPayload(
                    null, null, null, null, null, null, null, null);
        }

        String analysisContent = serializeOverallAnalysis(overallAnalysis);
        String meetingTitle =
                overallAnalysis.meetingInfo() != null
                        ? overallAnalysis.meetingInfo().title()
                        : null;
        String meetingPurpose =
                overallAnalysis.meetingInfo() != null
                        ? overallAnalysis.meetingInfo().purpose()
                        : null;
        String meetingDuration =
                overallAnalysis.meetingInfo() != null
                        ? overallAnalysis.meetingInfo().duration()
                        : null;
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
                applicationReasons);
    }

    // OverallAnalysis를 원본 JSON 문자열로 직렬화한다.
    private String serializeOverallAnalysis(
            TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis) {
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
    private List<Dialogue> buildDialogues(
            Meeting meeting,
            List<TranscribeApplicationRunResponse.TranscriptSegmentResponse> transcriptSegments) {
        if (transcriptSegments == null || transcriptSegments.isEmpty()) {
            return List.of();
        }

        Map<Long, Member> membersById =
                meeting.getMeetingMembers().stream()
                        .map(MeetingMember::getMember)
                        .collect(Collectors.toMap(Member::getId, Function.identity()));

        if (membersById.isEmpty()) {
            return List.of();
        }

        List<Dialogue> dialogues = new ArrayList<>();
        for (int index = 0; index < transcriptSegments.size(); index++) {
            TranscribeApplicationRunResponse.TranscriptSegmentResponse segment =
                    transcriptSegments.get(index);
            if (segment == null || segment.text() == null || segment.text().isBlank()) {
                continue;
            }

            Member member = segment.memberId() != null ? membersById.get(segment.memberId()) : null;
            if (member == null) {
                continue;
            }

            LocalDateTime speechDateTime =
                    resolveSpeechDateTime(meeting.getStartDateTime(), segment.startTime(), index);
            dialogues.add(Dialogue.create(meeting, member, segment.text().trim(), speechDateTime));
        }

        return dialogues;
    }

    // 전사 시작 시각 offset을 회의 시작 시각 기준 LocalDateTime으로 변환한다.
    private LocalDateTime resolveSpeechDateTime(
            LocalDateTime meetingStartDateTime, String offset, int fallbackIndex) {
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

    private record SavedApplications(
            Long decisionId,
            List<Application> savedApplications,
            List<TranscribeApplicationRunResponse.ApplicationResponse> sourceApplications) {

        private static SavedApplications empty(Long decisionId) {
            return new SavedApplications(decisionId, List.of(), List.of());
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
