package com.whylog.server.global.external.fast.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

import com.whylog.server.global.external.fast.FastApiInfo;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.CommitAnalyzeRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FastApiCommitClient extends FastApiClient {

    public FastApiCommitClient(RestClient.Builder restClientBuilder) {
        super(restClientBuilder);
    }

    public FastApiResponse<JsonNode> analyzeCommit(CommitAnalyzeRequest request) {
        return postJson(
                FastApiInfo.COMMIT_ANALYZE,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<JsonNode> getCommitAnalyzeRun(String runId) {
        return get(
                FastApiInfo.COMMIT_ANALYZE_RUN_STATUS,
                Map.of("run_id", runId),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public FastApiResponse<JsonNode> matchApplicationCommits(Map<String, Object> request) {
        return postJson(
                FastApiInfo.COMMIT_MATCH,
                request,
                new ParameterizedTypeReference<>() {
                }
        );
    }
}
