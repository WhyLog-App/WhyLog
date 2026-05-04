package com.whylog.server.domain.decision.controller;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.global.apiPayload.ApiResponse;
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
