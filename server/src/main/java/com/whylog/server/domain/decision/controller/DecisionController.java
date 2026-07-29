package com.whylog.server.domain.decision.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.decision.exception.DecisionErrorCode;
import com.whylog.server.domain.decision.service.DecisionCommitMatchService;
import com.whylog.server.domain.decision.service.DecisionQueryService;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/decisions")
@RequiredArgsConstructor
@Tag(name = "Decision", description = "결정사항 관련 API")
public class DecisionController {

    private final DecisionQueryService decisionQueryService;
    private final DecisionCommitMatchService decisionCommitMatchService;

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

    @PostMapping("/{decisionId}/commit/match")
    @Operation(
            summary = "결정사항 적용사항-커밋 추천 매칭",
            description = """
                    결정사항에 속한 적용사항별 커밋 추천 후보를 FastAPI에 요청하고 결과를 저장합니다.
                    회의 분석이 끝난 후 자동으로 1회 실행되며,
                    결정사항 페이지 추천 커밋 목록에 있는 새로고침시 해당 API를 호출하면 실행됩니다.
                    
                    결정사항이 포함된 회의의 팀 레포지토리 ID 목록을 생성해 전달합니다.
                    추천 개수는 적용사항별 최대 5개로 고정합니다.
                    이미 Spring 서버에서 적용사항에 연결된 커밋은 추천 응답과 저장 대상에서 제외합니다.
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "DECISION_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "REPOSITORY_NOT_FOUND")
    })
    public ApiResponse<JsonNode> matchApplicationCommits(
            @PathVariable Long decisionId) {
        return ApiResponse.onSuccess(decisionCommitMatchService.matchApplicationCommits(decisionId));
    }

    @PostMapping("/{decisionId}/commit/match/test")
    @Operation(
            summary = "결정사항 적용사항-커밋 추천 매칭 저장(테스트용)",
            description = """
                    FastAPI 호출 없이 전달받은 FastAPI 응답 JSON의 result를 추천 결과로 저장합니다.
                    
                    테스트용 API입니다. `result`를 포함한 전체 응답을 보내도 되고, result 객체만 보내도 됩니다.
                    이미 Spring 서버에서 적용사항에 연결된 커밋은 저장 대상에서 제외합니다.
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "DECISION_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "REPOSITORY_NOT_FOUND")
    })
    public ApiResponse<JsonNode> saveTestApplicationCommitMatches(
            @PathVariable Long decisionId,
            @RequestBody JsonNode fastApiResponse) {
        return ApiResponse.onSuccess(decisionCommitMatchService.saveTestApplicationCommitMatches(decisionId, fastApiResponse));
    }
}
