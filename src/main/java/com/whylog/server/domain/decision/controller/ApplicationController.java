package com.whylog.server.domain.decision.controller;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionRequest;
import com.whylog.server.domain.decision.exception.DecisionErrorCode;
import com.whylog.server.domain.decision.service.ApplicationCommandService;
import com.whylog.server.domain.decision.service.ApplicationQueryService;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    private final ApplicationCommandService applicationCommandService;
    private final ApplicationQueryService applicationQueryService;


    @GetMapping("/{applicationId}")
    @Operation(summary = "적용사항 상세 조회 API", description = "특정 적용사항의 상세 정보를 조회하는 API입니다. 적용사항 상세 조회 화면에서 적용사항 제목, 타임라인, 결정원문 맥락, 결정근거를 조회합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND")
    })
    public ApiResponse<ApplicationResponse.ApplicationDetailDTO> getApplication(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(applicationQueryService.getApplicationDetail(applicationId));
    }

    @GetMapping("/{applicationId}/recommended-commits")
    @Operation(summary = "추천 커밋 조회 API", description = "특정 적용사항의 추천 커밋 목록을 조회하는 API입니다.최신순으로 조회, 페이징 없습니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "COMMIT_NOT_FOUND")
    })
    public ApiResponse<List<ApplicationResponse.RecommendedCommitDTO>> getRecommendedCommits(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(applicationQueryService.getRecommendedCommits(applicationId));
    }

    @GetMapping("/{applicationId}/connected-commits")
    @Operation(summary = "연결된 커밋 조회 API", description = "특정 적용사항에 연결된 커밋 목록을 조회하는 API입니다.최신순으로 조회, 페이징 없습니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND"),
    })
    public ApiResponse<ApplicationResponse.ConnectedCommitListDTO> getConnectedCommits(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(applicationQueryService.getConnectedCommits(applicationId));
    }

    @GetMapping("/{applicationId}/status")
    @Operation(summary = "적용현황 조회 API", description = "특정 적용사항에 연결된 커밋 해시, 커밋 메시지, 연결된 커밋 개수를 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND")
    })
    public ApiResponse<ApplicationResponse.ApplicationStatusDTO> getApplicationStatus(
            @PathVariable Long applicationId) {
        return ApiResponse.onSuccess(applicationQueryService.getApplicationStatus(applicationId));
    }

    @PostMapping("/{applicationId}/commits")
    @Operation(summary = "커밋 연결 API", description = """
            적용사항에 커밋을 연결하는 API입니다.
            
            단일 연결과 다중 연결 모두 `commit_ids` 배열로 전달합니다.
            단건 연결 예시: `{ "commit_ids": [1] }`
            다건 연결 예시: `{ "commit_ids": [1, 2, 3] }`
            요청한 커밋 중 하나라도 이미 연결되어 있으면 전체 요청이 실패합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "COMMIT_NOT_FOUND"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_COMMIT_ALREADY_CONNECTED")
    })
    public ApiResponse<ApplicationResponse.CommitConnectionResponseDTO> connectCommit(
            @PathVariable Long applicationId,
            @Valid @RequestBody DecisionRequest.CommitConnectionDTO request) {
        return ApiResponse.onSuccess(applicationCommandService.connectCommit(applicationId, request));
    }

    @DeleteMapping("/{applicationId}/commits")
    @Operation(summary = "커밋 연결 해제 API", description = """
            적용사항에 연결된 커밋을 해제하는 API입니다.
            
            해제할 커밋 ID 하나를 `commit_id`로 전달합니다.
            예시: `{ "commit_id": 1 }`
            요청한 커밋이 연결되어 있지 않으면 요청이 실패합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_NOT_FOUND"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "APPLICATION_COMMIT_NOT_CONNECTED")
    })
    public ApiResponse<ApplicationResponse.CommitConnectionResponseDTO> disconnectCommit(
            @PathVariable Long applicationId,
            @Valid @RequestBody DecisionRequest.CommitDisconnectionDTO request) {
        return ApiResponse.onSuccess(applicationCommandService.disconnectCommit(applicationId, request));
    }

    @PostMapping("/{decisionId}/recommendations")
    @Operation(summary = "추천 결과 저장 API", description = "적용사항의 추천 결과를 저장하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = DecisionErrorCode.class, name = "DECISION_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "COMMIT_NOT_FOUND")
    })
    public ApiResponse<ApplicationResponse.RecommendedCommitDTO> saveRecommendation(
            @PathVariable Long decisionId,
            @Valid @RequestBody DecisionRequest.RecommendationDTO request) {
        return ApiResponse.onSuccess(null);
    }

}
