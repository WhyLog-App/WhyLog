package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * FastAPI 회의 분석 재추출에 넘길 전사 세그먼트입니다.
 *
 * <p>speaker와 isFinal은 FastAPI에서 필수 필드라 비우면 422가 납니다. 키 이름이 어긋나도 같은 결과라, 전역 Jackson 설정에 기대지 않고
 * snake_case를 명시합니다.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TranscriptSegmentPayload(
        Long messageId,
        String speaker,
        Long memberId,
        String startTime,
        String endTime,
        String text,
        Boolean isFinal) {}
