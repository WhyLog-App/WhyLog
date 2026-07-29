package com.whylog.server.global.external.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpMethod;

@Getter
@AllArgsConstructor
public enum FastApiInfo {

    READ_ROOT(HttpMethod.GET, "/", FastApiRequestBodyType.NONE),
    HEALTH_CHECK(HttpMethod.GET, "/health", FastApiRequestBodyType.NONE),

    TRANSCRIBE_AUDIO(HttpMethod.POST, "/api/transcribe", FastApiRequestBodyType.MULTIPART),
    TRANSCRIBE_APPLICATIONS(HttpMethod.POST, "/api/transcribe/applications", FastApiRequestBodyType.MULTIPART),
    TRANSCRIBE_APPLICATION_RUNS(HttpMethod.POST, "/api/transcribe/applications/runs", FastApiRequestBodyType.MULTIPART),
    TRANSCRIBE_APPLICATION_RUN_STATUS(HttpMethod.GET, "/api/transcribe/applications/runs/{run_id}", FastApiRequestBodyType.NONE),

    MEETING_ANALYSIS_EXTRACT(HttpMethod.POST, "/api/meeting-analysis/extract", FastApiRequestBodyType.JSON),
    MEETING_ANALYSIS_EMBEDDINGS(HttpMethod.POST, "/api/meeting-analysis/embeddings", FastApiRequestBodyType.JSON),

    COMMIT_ANALYZE(HttpMethod.POST, "/api/commit/analyze/runs", FastApiRequestBodyType.JSON),
    COMMIT_ANALYZE_RUN_STATUS(HttpMethod.GET, "/api/commit/analyze/runs/{run_id}", FastApiRequestBodyType.NONE),
    COMMIT_MATCH(HttpMethod.POST, "/api/commit/match", FastApiRequestBodyType.JSON);

    private final HttpMethod method;
    private final String path;
    private final FastApiRequestBodyType fastApiRequestBodyType;

}
