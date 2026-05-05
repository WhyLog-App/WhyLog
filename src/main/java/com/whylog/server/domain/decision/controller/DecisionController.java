package com.whylog.server.domain.decision.controller;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
@Tag(name = "Decision", description = "결정사항 관련 API")
public class DecisionController {

    private final DecisionQueryService decisionQueryService;

    @GetMapping("/{decisionId}")
    @Operation(summary = "결정사항 상세 조회 API", description = "특정 결정사항의 상세 정보를 조회하는 API입니다. 적용사항 상세 조회 화면에서 상단의 결정사항 상세 정보를 조회합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "DECISION_NOT_FOUND")
    })
    public ApiResponse<DecisionResponse.DecisionDetailDTO> getDecision(
            @PathVariable Long decisionId) {
        return ApiResponse.onSuccess(decisionQueryService.getDecisionDetail(decisionId));
    }

    @GetMapping("/{decisionId}/reliability")
    @Operation(summary = "신뢰도 조회 API", description = "특정 결정사항의 신뢰도 정보를 조회하는 API입니다.")
    public ApiResponse<DecisionResponse.ReliabilityDTO> getReliability(
            @PathVariable Long decisionId) {
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "추천 커밋 조회 API", description = "특정 적용사항의 추천 커밋 목록을 조회하는 API입니다.")
    public ApiResponse<List<ApplicationResponse.RecommendedCommitDTO>> getRecommendedCommits(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{applicationId}/connected-commits")
    @Operation(summary = "연결된 커밋 조회 API", description = "특정 적용사항에 연결된 커밋 목록을 조회하는 API입니다.")
    public ApiResponse<List<ApplicationResponse.ConnectedCommitDTO>> getConnectedCommits(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(null);
    }
}
