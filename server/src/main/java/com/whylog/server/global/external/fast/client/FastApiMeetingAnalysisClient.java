package com.whylog.server.global.external.fast.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.ApplicationEmbeddingsRequest;
import com.whylog.server.global.external.fast.dto.request.MeetingAnalysisRequest;
import com.whylog.server.global.external.fast.dto.response.ApplicationEmbeddingsResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FastApiMeetingAnalysisClient extends FastApiClient {

    public FastApiMeetingAnalysisClient(RestClient.Builder restClientBuilder) {
        super(restClientBuilder);
    }

    public FastApiResponse<JsonNode> extractMeetingAnalysis(MeetingAnalysisRequest request) {
        return postJson(
                FastApiInfo.MEETING_ANALYSIS_EXTRACT,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<ApplicationEmbeddingsResponse> createApplicationEmbeddings(ApplicationEmbeddingsRequest request) {
        return postJson(
                FastApiInfo.MEETING_ANALYSIS_EMBEDDINGS,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
