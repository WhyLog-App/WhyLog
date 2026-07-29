package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.whylog.server.global.external.fast.dto.response.TranscribeApplicationRunResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "FastAPI 회의 분석 임베딩 요청")
public record ApplicationEmbeddingsRequest(
        @Schema(description = "회의 ID", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID", nullable = true)
        String projectId,
        @Schema(description = "회의 분석 결과", nullable = true)
        AnalysisResultPayload analysisResult
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "회의 분석 결과 payload")
    public record AnalysisResultPayload(
            @Schema(description = "전체 분석 결과", nullable = true)
            TranscribeApplicationRunResponse.OverallAnalysisResponse overallAnalysis,
            @Schema(description = "적용사항 목록", nullable = true)
            List<ApplicationPayload> applications,
            @Schema(description = "기타 언급 목록", nullable = true)
            List<String> otherMentions
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "적용사항 payload")
    public record ApplicationPayload(
            @Schema(description = "적용사항 ID", nullable = true)
            Long applicationId,
            @Schema(description = "적용사항 제목", nullable = true)
            String applicationTitle,
            @Schema(description = "적용사항 사유 목록", nullable = true)
            List<String> applicationReasons,
            @Schema(description = "적용사항 타임라인", nullable = true)
            List<TimelinePayload> timeline
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "적용사항 타임라인 payload")
    public record TimelinePayload(
            @Schema(description = "타임스탬프", nullable = true)
            String timestamp,
            @Schema(description = "단계", nullable = true)
            String step,
            @Schema(description = "발화자 멤버 ID", nullable = true)
            Long memberId,
            @Schema(description = "내용", nullable = true)
            String content,
            @Schema(description = "원문 발화", nullable = true)
            String utterance
    ) {
    }
}
