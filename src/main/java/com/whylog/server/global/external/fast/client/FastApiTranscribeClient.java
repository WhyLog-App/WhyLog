package com.whylog.server.global.external.fast.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.TranscribeRequest;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunCreateResponse;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FastApiTranscribeClient extends FastApiClient {

    public FastApiResponse<JsonNode> transcribeAudio(Resource audio,
                                                     String filename,
                                                     String contentType,
                                                     Integer numSpeakers) {
        return transcribeAudio(audio, filename, contentType, numSpeakers, null, null);
    }

    public FastApiResponse<JsonNode> transcribeAudio(TranscribeRequest request) {
        return transcribeAudio(
                request.audio().resource(),
                request.audio().filename(),
                request.audio().contentType(),
                request.numSpeakers(),
                request.meetingId(),
                request.projectId()
        );
    }

    public FastApiResponse<JsonNode> transcribeAudio(Resource audio,
                                                     String filename,
                                                     String contentType,
                                                     Integer numSpeakers,
                                                     String meetingId,
                                                     String projectId) {
        return postMultipart(
                FastApiInfo.TRANSCRIBE_AUDIO,
                buildTextParts(numSpeakers, meetingId, projectId),
                new FastApiBinaryPart("audio", audio, filename, contentType),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<JsonNode> transcribeApplications(Resource audio,
                                                            String filename,
                                                            String contentType,
                                                            Integer numSpeakers,
                                                            String meetingId,
                                                            String projectId) {
        return postMultipart(
                FastApiInfo.TRANSCRIBE_APPLICATIONS,
                buildTextParts(numSpeakers, meetingId, projectId),
                new FastApiBinaryPart("audio", audio, filename, contentType),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<JsonNode> transcribeApplications(TranscribeRequest request) {
        return transcribeApplications(
                request.audio().resource(),
                request.audio().filename(),
                request.audio().contentType(),
                request.numSpeakers(),
                request.meetingId(),
                request.projectId()
        );
    }

    public FastApiResponse<TranscribeApplicationRunCreateResponse> createTranscribeApplicationRun(Resource audio,
                                                                                                  String filename,
                                                                                                  String contentType,
                                                                                                  Integer numSpeakers,
                                                                                                  String meetingId,
                                                                                                  String projectId) {
        return postMultipart(
                FastApiInfo.TRANSCRIBE_APPLICATION_RUNS,
                buildTextParts(numSpeakers, meetingId, projectId),
                new FastApiBinaryPart("audio", audio, filename, contentType),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<TranscribeApplicationRunCreateResponse> createTranscribeApplicationRun(TranscribeRequest request) {
        return createTranscribeApplicationRun(
                request.audio().resource(),
                request.audio().filename(),
                request.audio().contentType(),
                request.numSpeakers(),
                request.meetingId(),
                request.projectId()
        );
    }

    public FastApiResponse<TranscribeApplicationRunResponse> getTranscribeApplicationRun(String runId) {
        return get(
                FastApiInfo.TRANSCRIBE_APPLICATION_RUN_STATUS,
                Map.of("run_id", runId),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    private Map<String, Object> buildTextParts(Integer numSpeakers, String meetingId, String projectId) {
        Map<String, Object> parts = new LinkedHashMap<>();
        if (numSpeakers != null) {
            parts.put("num_speakers", numSpeakers);
        }
        if (meetingId != null) {
            parts.put("meeting_id", meetingId);
        }
        if (projectId != null) {
            parts.put("project_id", projectId);
        }
        return parts;
    }
}
