package com.whylog.server.domain.git.controller;

import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Git", description = "깃 관련 API")
public class GitController {

    @GetMapping("/teams/{teamId}/repositories")
    @Operation(summary = "레포 목록 조회 API", description = "특정 팀의 레포 목록을 조회하는 API입니다.")
    public ApiResponse<List<GitResponse.RepositoryDTO>> getRepositories(
            @PathVariable Long teamId) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/teams/{teamId}/repositories")
    @Operation(summary = "레포 추가 API", description = "팀에 새로운 레포를 추가하는 API입니다.")
    public ApiResponse<GitResponse.RepositoryCreateResponseDTO> createRepository(
            @PathVariable Long teamId,
            @Valid @RequestBody GitRequest.RepositoryCreateDTO request) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/repositories/{repositoryId}/commits")
    @Operation(summary = "커밋 목록 조회 API", description = "특정 레포의 커밋 목록을 조회하는 API입니다.")
    public ApiResponse<List<GitResponse.CommitDTO>> getCommits(
            @PathVariable Long repositoryId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/commits/{commitId}")
    @Operation(summary = "커밋 상세 조회 API", description = "특정 커밋의 상세 정보를 조회하는 API입니다.")
    public ApiResponse<GitResponse.CommitDetailDTO> getCommitDetail(
            @PathVariable Long commitId) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/repositories/{repositoryId}/sync")
    @Operation(summary = "레포 동기화 API", description = "레포를 동기화하여 최신 커밋 정보를 가져오는 API입니다.")
    public ApiResponse<GitResponse.RepositorySyncResponseDTO> syncRepository(
            @PathVariable Long repositoryId) {
        return ApiResponse.onSuccess(null);
    }
}
