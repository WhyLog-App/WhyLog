package com.whylog.server.global.external.fast.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "FastAPI 전사+회의 분석 비동기 실행 상태 조회 응답")
public record TranscribeApplicationRunResponse(
        @Schema(description = "비동기 실행 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String runId,
        @Schema(description = "실행 상태", example = "completed")
        String status,
        @Schema(description = "실행 단계", example = "applications_ready")
        String phase,
        @Schema(description = "회의 ID", example = "meeting-123", nullable = true)
        String meetingId,
        @Schema(description = "프로젝트 ID", example = "project-abc", nullable = true)
        String projectId,
        @Schema(description = "제출 시각", nullable = true)
        OffsetDateTime submittedAt,
        @Schema(description = "시작 시각", nullable = true)
        OffsetDateTime startedAt,
        @Schema(description = "종료 시각", nullable = true)
        OffsetDateTime finishedAt,
        @Schema(description = "실행 실패 사유", nullable = true)
        String error,
        @Schema(description = "최종 또는 중간 결과", nullable = true)
        TranscribeApplicationRunResult result
) {

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "전사+회의 분석 실행 결과")
    public record TranscribeApplicationRunResult(
            @Schema(description = "회의 ID", example = "meeting-123", nullable = true)
            String meetingId,
            @Schema(description = "프로젝트 ID", example = "project-abc", nullable = true)
            String projectId,
            @Schema(description = "전사 세그먼트 목록", nullable = true)
            List<TranscriptSegmentResponse> transcriptSegments,
            @Schema(description = "회의 분석 결과", nullable = true)
            AnalysisResultResponse analysisResult
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "전사 세그먼트")
    public record TranscriptSegmentResponse(
            @Schema(description = "메시지 ID", example = "1", nullable = true)
            Long messageId,
            @Schema(description = "화자명", example = "Speaker 0", nullable = true)
            String speaker,
            @Schema(description = "시작 시각", example = "00:00:00", nullable = true)
            String startTime,
            @Schema(description = "종료 시각", example = "00:00:04", nullable = true)
            String endTime,
            @Schema(description = "전사 텍스트", example = "Swagger 에러 응답 예시가 부족합니다.", nullable = true)
            String text,
            @Schema(description = "최종 전사 여부", example = "true", nullable = true)
            Boolean isFinal
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "회의 분석 결과")
    public record AnalysisResultResponse(
            @Schema(description = "전체 분석 결과", nullable = true)
            OverallAnalysisResponse overallAnalysis,
            @Schema(description = "적용사항 목록", nullable = true)
            List<ApplicationResponse> applications,
            @Schema(description = "기타 언급 목록", nullable = true)
            List<String> otherMentions
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "전체 분석 결과")
    public record OverallAnalysisResponse(
            @Schema(description = "회의 정보", nullable = true)
            MeetingInfoResponse meetingInfo,
            @Schema(description = "토픽 목록", nullable = true)
            List<String> topics,
            @Schema(description = "핵심 맥락", nullable = true)
            List<String> coreContext,
            @Schema(description = "적용사항 제목 목록", nullable = true)
            List<String> applicationTitles,
            @Schema(description = "적용사항 사유 목록", nullable = true)
            List<String> applicationReasons
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "회의 정보")
    public record MeetingInfoResponse(
            @Schema(description = "회의 제목", nullable = true)
            String title,
            @Schema(description = "회의 목적", nullable = true)
            String purpose,
            @Schema(description = "회의 길이", example = "00:12:30", nullable = true)
            String duration
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "적용사항")
    public record ApplicationResponse(
            @Schema(description = "적용사항 ID", example = "null", nullable = true)
            Long applicationId,
            @Schema(description = "적용사항 제목", nullable = true)
            String applicationTitle,
            @Schema(description = "적용사항 사유 목록", nullable = true)
            List<String> applicationReasons,
            @Schema(description = "타임라인", nullable = true)
            List<TimelineResponse> timeline
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "적용사항 타임라인")
    public record TimelineResponse(
            @Schema(description = "타임스탬프", example = "00:03:12", nullable = true)
            String timestamp,
            @Schema(description = "단계", example = "이슈제기", nullable = true)
            String step,
            @Schema(description = "화자 ID", example = "Speaker 0", nullable = true)
            String speakerId,
            @Schema(description = "내용", nullable = true)
            String content,
            @Schema(description = "원문 발화", nullable = true)
            String utterance
    ) {
    }
}
