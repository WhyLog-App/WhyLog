package com.whylog.server.global.external.fast.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.MeetingAnalysisRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class FastApiMeetingAnalysisClient extends FastApiClient {

    public FastApiResponse<JsonNode> extractMeetingAnalysis(MeetingAnalysisRequest request) {
        return postJson(
                FastApiInfo.MEETING_ANALYSIS_EXTRACT,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<JsonNode> createApplicationEmbeddings(Map<String, Object> request) {
        return postJson(
                FastApiInfo.MEETING_ANALYSIS_EMBEDDINGS,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
