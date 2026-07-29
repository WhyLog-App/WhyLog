package com.whylog.server.domain.git.controller;

import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.service.GitCommandService;
import com.whylog.server.domain.git.service.GitQueryService;
import com.whylog.server.domain.team.exception.TeamErrorCode;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Git", description = "깃 관련 API")
public class GitController {

    private final GitCommandService gitCommandService;
    private final GitQueryService gitQueryService;

    @PostMapping("/github/token")
    @Operation(
            summary = "GitHub Access Token 등록 API",
            description = """
                    GitHub API를 사용하기 위한 Personal Access Token을 등록하는 API입니다.
                    - 사용자가 GitHub에서 발급한 Personal Access Token을 저장합니다.
                    - 저장된 토큰은 이후 레포지토리 동기화 시 자동으로 사용됩니다.
                    - GitHub Personal Access Token 발급 필요 (repo 권한 포함)
                    - GitHub Settings > Developer settings > Personal access tokens에서 발급
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_INVALID"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_INTERNAL_SERVER_ERROR")
    })
    public ApiResponse<GitResponse.GitHubTokenResponseDTO> registerGitHubToken(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody GitRequest.GitHubTokenDTO request) {
        return ApiResponse.onSuccess(gitCommandService.registerGitHubToken(memberId, request.getAccessToken()));
    }

    @DeleteMapping("/github/token")
    @Operation(
            summary = "GitHub Access Token 삭제 API",
            description = "현재 로그인한 사용자의 GitHub Access Token을 삭제합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST")
    })
    public ApiResponse<GitResponse.GitHubTokenDeleteResponseDTO> deleteGitHubToken(
            @Parameter(hidden = true) @CurrentMember Long memberId) {
        return ApiResponse.onSuccess(gitCommandService.deleteGitHubToken(memberId));
    }

    @GetMapping("/github/token/status")
    @Operation(
            summary = "GitHub Access Token 등록 여부 조회 API",
            description = "현재 로그인한 사용자의 GitHub Access Token 등록 여부를 true/false로 조회합니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST")
    })
    public ApiResponse<GitResponse.GitHubTokenStatusResponseDTO> getGitHubTokenStatus(
            @Parameter(hidden = true) @CurrentMember Long memberId) {
        return ApiResponse.onSuccess(gitQueryService.getGitHubTokenStatus(memberId));
    }

    @GetMapping("/teams/{teamId}/repositories")
    @Operation(
            summary = "팀의 연동된 레포지토리 목록 조회",
            description = """
                    팀에 연동된 GitHub 레포지토리 목록을 조회합니다.(페이징 없음)
                    
                    정렬 순서:
                    1. 최근 동기화한 레포지토리 (동기화 시간 최신순)
                    2. 동기화된 적 없는 레포지토리 (추가한순)
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_NOT_FOUND")
    })
    public ApiResponse<List<GitResponse.RepositoryDTO>> getRepositories(
            @PathVariable Long teamId) {
        return ApiResponse.onSuccess(
                gitQueryService.getRepositories(teamId).stream()
                        .map(GitResponse.RepositoryDTO::from)
                        .toList()
        );
    }

    @PostMapping("/teams/{teamId}/repositories")
    @Operation(
            summary = "GitHub 레포지토리 추가",
            description = "GitHub 레포지토리를 팀에 연동합니다. 등록 시에는 레포 정보만 저장되며, 커밋은 동기화 API를 호출할 때 수집됩니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "INVALID_GITHUB_URL"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_NOT_REGISTERED"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_EXPIRED")
    })
    public ApiResponse<GitResponse.RepositoryCreateResponseDTO> createRepository(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long teamId,
            @Valid @RequestBody GitRequest.RepositoryCreateDTO request) {
        var repository = gitCommandService.createRepository(memberId, teamId, request);
        return ApiResponse.onSuccess(GitResponse.RepositoryCreateResponseDTO.from(repository));
    }

    @PostMapping("/repositories/{repositoryId}/sync")
    @Operation(
            summary = "GitHub 레포지토리 동기화",
            description = "등록된 레포지토리의 최신 커밋을 DB에 저장합니다. 마지막 동기화 이후의 새 커밋만 저장되며 Merge 커밋은 제외됩니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "REPOSITORY_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_NOT_REGISTERED"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_EXPIRED")
    })
    public ApiResponse<GitResponse.RepositorySyncResponseDTO> syncRepository(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long repositoryId) {

        gitCommandService.syncRepository(memberId, repositoryId);
        return ApiResponse.onSuccess(GitResponse.RepositorySyncResponseDTO.from(repositoryId));
    }

    @DeleteMapping("/repositories/{repositoryId}")
    @Operation(
            summary = "GitHub 레포지토리 삭제",
            description = """
                    등록된 레포지토리를 삭제합니다.
                    
                    레포지토리를 삭제하면 관련된 커밋, 커밋 분석, 연결/추천 커밋 데이터도 함께 삭제됩니다.
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "REPOSITORY_NOT_FOUND")
    })
    public ApiResponse<GitResponse.RepositoryDeleteResponseDTO> deleteRepository(
            @PathVariable Long repositoryId) {
        return ApiResponse.onSuccess(gitCommandService.deleteRepository(repositoryId));
    }

    @GetMapping("/repositories/{repositoryId}/commits")
    @Operation(
            summary = "커밋 목록 조회 (커서 기반 무한스크롤)",
            description = """
                    10개씩 커밋을 조회합니다.
                    
                    각 커밋에는 기본 정보와 함께 연결된 적용사항 정보가 포함됩니다.
                    커밋이 적용사항에 연결되어 있지 않다면 `connectedApplication`은 null로 반환됩니다.
                    
                    📌 사용 방법:
                    1. 첫 요청: cursor 파라미터 없음
                    2. 응답의 hasNext가 true면, nextCursorId를 cursor로 다시 요청
                    3. hasNext가 false가 나올 때까지 반복
                    
                    💡 응답 필드:
                    - totalCommitCount: 현재 레포지토리에 저장된 전체 커밋 개수
                    - connectedCommitCount: 적용사항에 연결된 커밋 개수
                    - unconnectedCommitCount: 아직 적용사항에 연결되지 않은 커밋 개수
                    - hasNext: 다음 페이지 존재 여부 (더 불러올 커밋이 있으면 true)
                    - nextCursorId: 다음 요청에 사용할 커서 ID
                    - isFirst: 첫 페이지 여부
                    - connectedApplication: 커밋에 연결된 적용사항 정보 (없으면 null)
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "REPOSITORY_NOT_FOUND")
    })
    public ApiResponse<GitResponse.CommitListResponseDTO> getCommitsCursor(
            @PathVariable Long repositoryId,
            @Parameter(description = "이전 조회의 마지막 커밋 ID (첫 요청 시 생략)")
            @RequestParam(required = false) Long cursor) {
        return ApiResponse.onSuccess(gitQueryService.getCommitListResponse(repositoryId, cursor));
    }

    @GetMapping("/repositories/{repositoryId}/commits/{commitHash}")
    @Operation(
            summary = "커밋 상세 조회",
            description = """
                    커밋의 전체 정보와 변경된 파일 목록을 조회합니다.
                    
                    - DB에서 가져오는 정보: hash, message, authorName, authorEmail, authorProfileImage, dateTime, description, changedFileCount
                    - (참고) authorName, authorEmail, authorProfileImage는 깃허브에서 가져와서 저장한 정보입니다.
                    - GitHub API에서 실시간 조회: changedFileList (변경된 파일 및 코드)
                    
                    ⚙️ 처리 방식:
                    - GitHub API 호출 완료 후 응답 (비동기 아님)
                    - GitHub API 호출 실패 시 변경 파일 리스트는 빈 배열로 반환
                    
                    💡 프론트 구현시 참고 사항:
                    - changedCode를 react-diff-viewer-continued 라이브러리 사용하면 될거같으니 참고해주세요!
                    """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "COMMIT_NOT_FOUND"),
            @ApiErrorCodeExample(value = GitErrorCode.class, name = "GITHUB_TOKEN_NOT_REGISTERED")
    })
    public ApiResponse<GitResponse.CommitDetailDTO> getCommitDetail(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long repositoryId,
            @PathVariable String commitHash) {
        GitResponse.CommitDetailDTO commitDetail = gitQueryService.getCommitByHash(memberId, repositoryId, commitHash);
        return ApiResponse.onSuccess(commitDetail);
    }
}
