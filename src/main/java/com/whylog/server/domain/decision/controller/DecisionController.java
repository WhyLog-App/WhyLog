package com.whylog.server.domain.decision.controller;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.decision.exception.DecisionErrorCode;
import com.whylog.server.domain.decision.service.DecisionQueryService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
@Tag(name = "Decision", description = "결정사항 관련 API")
public class DecisionController {

    private final DecisionQueryService decisionQueryService;

    @GetMapping("/{decisionId}/reliability")
    @Operation(summary = "신뢰도 조회 API", description = "특정 결정사항의 신뢰도 정보를 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "DECISION_NOT_FOUND")
    })
    public ApiResponse<DecisionResponse.ReliabilityDTO> getReliability(
            @PathVariable Long decisionId) {
        return ApiResponse.onSuccess(decisionQueryService.getReliability(decisionId));
    }
}
