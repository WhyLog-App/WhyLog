package com.whylog.server.domain.decision.controller;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionRequest;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Application", description = "적용사항 관련 API")
public class ApplicationController {


    @GetMapping("/{applicationId}")
    @Operation(summary = "적용사항 상세 조회 API", description = "특정 적용사항의 상세 정보를 조회하는 API입니다.")
    public ApiResponse<ApplicationResponse.ApplicationDetailDTO> getApplication(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/{applicationId}/recommended-commits")
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

    @PostMapping("/{applicationId}/commits")
    @Operation(summary = "커밋 연결 API", description = "적용사항에 커밋을 연결하는 API입니다.")
    public ApiResponse<ApplicationResponse.CommitConnectionResponseDTO> connectCommit(
            @PathVariable Long applicationId,
            @RequestBody DecisionRequest.CommitConnectionDTO request) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{decisionId}/recommendations")
    @Operation(summary = "추천 결과 저장 API", description = "적용사항의 추천 결과를 저장하는 API입니다.")
    public ApiResponse<ApplicationResponse.RecommendedCommitDTO> saveRecommendation(
            @PathVariable Long decisionId,
            @RequestBody DecisionRequest.RecommendationDTO request) {
        return ApiResponse.onSuccess(null);
    }

}
