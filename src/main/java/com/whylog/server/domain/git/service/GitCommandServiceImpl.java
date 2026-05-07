package com.whylog.server.domain.git.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.entity.CommitAnalysis;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.Repository;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.exception.GitTokenNotRegisteredException;
import com.whylog.server.domain.git.exception.RepositoryNotFoundException;
import com.whylog.server.domain.git.repository.CommitAnalysisRepository;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.domain.git.repository.RepositoryRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.external.fast.client.FastApiCommitClient;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.ChangedFile;
import com.whylog.server.global.external.fast.dto.request.CommitAnalyzeRequest;
import com.whylog.server.global.external.fast.exception.FastApiErrorCode;
import com.whylog.server.global.external.fast.exception.FastApiException;
import com.whylog.server.global.util.github.GitHubUtil;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.service.TeamUseCase;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.HttpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitCommandServiceImpl implements GitCommandService {

    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final CommitAnalysisRepository commitAnalysisRepository;
    private final FastApiCommitClient fastApiCommitClient;
    private final TeamUseCase teamUseCase;
    private final MemberUseCase memberUseCase;

    /**
     * 사용자의 GitHub Access Token을 등록합니다.
     * 등록 전에 token의 유효성을 검증합니다.
     */
    @Override
    @Transactional
    public GitResponse.GitHubTokenResponseDTO registerGitHubToken(Long memberId, String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) throw new ParameterRequiredException();

        // GitHub token 유효성 검증 (401 응답 시 예외 발생)
        GitHubUtil.validateGitHubToken(accessToken);

        Member member = memberUseCase.findMemberById(memberId);
        member.setGithubAccessToken(accessToken);

        return GitResponse.GitHubTokenResponseDTO.builder()
                .accessToken(accessToken)
                .build();
    }

    /**
     * 팀에 새로운 레포지토리를 추가합니다.
     */
    @Override
    @Transactional
    public Repository createRepository(Long memberId, Long teamId, GitRequest.RepositoryCreateDTO request) {
        if (teamId == null) throw new ParameterRequiredException();

        // 현재 사용자의 GitHub Token 조회
        Member member = memberUseCase.findMemberById(memberId);

        // token 존재 및 유효성 검증
        if (!member.hasGithubToken()) {
            throw new GitTokenNotRegisteredException();
        }

        try {
            GitHubUtil.validateGitHubToken(member.getGithubAccessToken());
            // 레포 URL 유효성 검증 및 GitHub에서 존재 여부 확인
            GitHubUtil.validateRepositoryExists(request.getUrl(), member.getGithubAccessToken());
        } catch (ErrorHandler e) {
            if (e.getCode() == GitErrorCode.GITHUB_TOKEN_EXPIRED) {
                invalidateGitHubToken(memberId);
            }
            throw e;
        }

        Team team = teamUseCase.findTeamById(teamId);
        Repository repository = Repository.create(request.getName(), request.getUrl(), team);
        return repositoryRepository.save(repository);
    }

    /**
     * 레포지토리를 동기화합니다 (커밋 저장)
     */
    @Override
    @Transactional
    public GitResponse.RepositorySyncResponseDTO syncRepository(Long memberId, Long repositoryId) {
        if (memberId == null) throw new ParameterRequiredException();
        if (repositoryId == null) throw new ParameterRequiredException();

        // 사용자의 GitHub Token 조회 및 검증
        Member member = memberUseCase.findMemberById(memberId);
        if (!member.hasGithubToken()) {
            throw new GitTokenNotRegisteredException();
        }

        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        try {
            GitHub gitHub = GitHubUtil.createGitHubInstance(member.getGithubAccessToken());
            String repoPath = GitHubUtil.extractRepoPath(repository.getUrl());
            GHRepository ghRepository = gitHub.getRepository(repoPath);

            // 마지막 동기화 시간 이후의 커밋만 저장
            var lastSyncedAt = repository.getLastSyncedAt();
            syncCommits(ghRepository, repository, lastSyncedAt);

            // 동기화 시간 업데이트
            repository.updateLastSyncedAt(LocalDateTime.now());

        } catch (HttpException e) {
            if (e.getResponseCode() == 401) {
                invalidateGitHubToken(memberId); // DB에서 토큰 삭제
                throw new ErrorHandler(GitErrorCode.GITHUB_TOKEN_EXPIRED);
            }
            GitHubUtil.handleHttpException(e);
        } catch (IOException e) {
            throw new RepositoryNotFoundException();
        }

        return GitResponse.RepositorySyncResponseDTO.builder()
                .repositoryId(repositoryId)
                .build();
    }

    /**
     * GitHub에서 마지막 동기화 시간 이후의 커밋을 조회하고 DB에 일괄 저장합니다.
     */
    private void syncCommits(GHRepository ghRepository, Repository repository, LocalDateTime lastSyncedAt) throws IOException {

        var commitQuery = ghRepository.queryCommits();

        if (lastSyncedAt != null) {
            Date sinceDate = Date.from(
                    lastSyncedAt.atZone(ZoneId.systemDefault()).toInstant()
            );
            commitQuery.since(sinceDate);
        }

        List<GHCommit> allGitHubCommits = commitQuery.list().toList();

        List<String> allHashes = allGitHubCommits.stream()
                .map(GHCommit::getSHA1)
                .toList();

        Set<String> existingHashes = allHashes.isEmpty()
                ? Collections.emptySet()
                : commitRepository.findExistingHashes(repository.getId(), allHashes);

        List<Commit> newCommits = allGitHubCommits.stream()
                .filter(ghCommit -> !existingHashes.contains(ghCommit.getSHA1()))
                .map(ghCommit -> {
                    try {
                        var shortInfo = ghCommit.getCommitShortInfo();
                        String message = shortInfo.getMessage();

                        if (ghCommit.getParentSHA1s().size() > 1) return null;

                        String authorProfileImage = (ghCommit.getAuthor() != null)
                                ? ghCommit.getAuthor().getAvatarUrl() : null;

                        GitRequest.CommitCreateDTO dto = GitRequest.CommitCreateDTO.builder()
                                .hash(ghCommit.getSHA1())
                                .message(message)
                                .authorName(shortInfo.getAuthor().getName())
                                .authorEmail(shortInfo.getAuthor().getEmail())
                                .authorProfileImage(authorProfileImage)
                                .dateTime(shortInfo.getAuthor().getDate()
                                        .toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime())
                                .addedLines(ghCommit.getLinesAdded())
                                .deletedLines(ghCommit.getLinesDeleted())
                                .build();

                        return Commit.create(dto, repository);
                    } catch (Exception e) {
                        log.warn("커밋 변환 실패 [{}]: {}", ghCommit.getSHA1(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (!newCommits.isEmpty()) {
            List<Commit> savedCommits = commitRepository.saveAllAndFlush(newCommits);
            triggerCommitAnalyzeRunsAfterCommit(ghRepository, repository, savedCommits);
            log.info("새로운 커밋 {}개 동기화 완료: {}", newCommits.size(), ghRepository.getFullName());
        } else {
            log.info("새로 동기화할 커밋이 없습니다: {}", ghRepository.getFullName());
        }
    }

    /**
     * 커밋 저장 커밋 이후에 분석 run 생성을 비동기로 시작합니다.
     */
    private void triggerCommitAnalyzeRunsAfterCommit(GHRepository ghRepository, Repository repository, List<Commit> commits) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    triggerCommitAnalyzeRunsAsync(ghRepository, repository, commits);
                }
            });
            return;
        }

        triggerCommitAnalyzeRunsAsync(ghRepository, repository, commits);
    }

    /**
     * 저장된 커밋들에 대해 FastAPI 분석 run 생성을 비동기로 순차 실행합니다.
     */
    private void triggerCommitAnalyzeRunsAsync(GHRepository ghRepository, Repository repository, List<Commit> commits) {
        CompletableFuture.runAsync(() -> commits.forEach(commit -> createCommitAnalyzeRun(ghRepository, repository, commit)))
                .exceptionally(ex -> {
                    log.warn("커밋 분석 run 생성 비동기 작업 실패: repositoryId={}", repository.getId(), ex);
                    return null;
                });
    }

    /**
     * 커밋별 변경 파일을 수집해 FastAPI 분석 run을 생성하고 완료 결과를 저장합니다.
     */
    private void createCommitAnalyzeRun(GHRepository ghRepository, Repository repository, Commit commit) {
        try {
            GHCommit ghCommit = ghRepository.getCommit(commit.getHash());
            List<ChangedFile> changedFiles = ghCommit.listFiles().toList().stream()
                    .map(this::toChangedFile)
                    .toList();

            CommitAnalyzeRequest request = new CommitAnalyzeRequest(
                    null,
                    commit.getHash(),
                    repository.getId().intValue(),
                    commit.getMessage(),
                    changedFiles
            );

            FastApiResponse<JsonNode> createResponse = fastApiCommitClient.analyzeCommit(request);
            JsonNode createResult = requireResult(createResponse);
            String runId = readText(createResult, "run_id");
            if (runId == null || runId.isBlank()) {
                throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
            }

            JsonNode runResult = pollCommitAnalyzeRun(runId);
            saveCommitAnalysis(commit, runResult);
        } catch (Exception e) {
            log.warn("커밋 분석 run 생성 실패: commitHash={}", commit.getHash(), e);
        }
    }

    /**
     * FastAPI 분석 run 상태를 폴링해 summary가 준비된 최종 결과를 가져옵니다.
     */
    private JsonNode pollCommitAnalyzeRun(String runId) {
        for (int attempt = 1; attempt <= 120; attempt++) {
            FastApiResponse<JsonNode> response = fastApiCommitClient.getCommitAnalyzeRun(runId);
            JsonNode runResult = response != null ? response.result() : null;

            if (runResult == null) {
                sleep(3000L);
                continue;
            }

            String status = readText(runResult, "status");
            String phase = readText(runResult, "phase");
            String summary = readNestedText(runResult, "result", "summary");

            if (isFailed(status, phase)) {
                throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED);
            }

            if (summary != null && !summary.isBlank()) {
                return runResult;
            }

            sleep(3000L);
        }

        throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED);
    }

    /**
     * 완료된 분석 결과에서 summary를 upsert 저장합니다.
     */
    private void saveCommitAnalysis(Commit commit, JsonNode runResult) {
        String summary = readNestedText(runResult, "result", "summary");

        if (summary == null || summary.isBlank()) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        CommitAnalysis commitAnalysis = commitAnalysisRepository.findByCommitId(commit.getId())
                .orElseGet(() -> CommitAnalysis.create(commit));
        commitAnalysis.updateSummary(summary);
        commitAnalysisRepository.save(commitAnalysis);

        log.info("커밋 분석 저장 완료: commitHash={}", commit.getHash());
    }

    /**
     * FastAPI run 상태가 실패인지 확인합니다.
     */
    private boolean isFailed(String status, String phase) {
        return "failed".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(phase);
    }

    /**
     * JSON 필드의 문자열 값을 안전하게 읽습니다.
     */
    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node != null ? node.get(fieldName) : null;
        return value != null ? value.asText(null) : null;
    }

    /**
     * 중첩 JSON 필드의 문자열 값을 안전하게 읽습니다.
     */
    private String readNestedText(JsonNode node, String parentFieldName, String childFieldName) {
        JsonNode parent = node != null ? node.get(parentFieldName) : null;
        JsonNode child = parent != null ? parent.get(childFieldName) : null;
        return child != null ? child.asText(null) : null;
    }

    /**
     * FastAPI 응답의 result가 비어 있으면 예외를 던집니다.
     */
    private <T> T requireResult(FastApiResponse<T> response) {
        if (response == null || response.result() == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }
        return response.result();
    }

    /**
     * GitHub 커밋 파일 정보를 FastAPI 요청용 changed file로 변환합니다.
     */
    private ChangedFile toChangedFile(GHCommit.File file) {
        return new ChangedFile(
                file.getFileName(),
                file.getPatch() != null ? file.getPatch() : ""
        );
    }

    /**
     * 폴링 사이에 잠시 대기합니다.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED, e);
        }
    }

    /**
     * GitHub Token 만료 시 처리합니다 (API 401 에러 감지)
     * token을 초기화하여 사용자가 재인증하도록 유도합니다.
     */
    @Override
    @Transactional
    public void invalidateGitHubToken(Long memberId) {
        if (memberId == null) throw new ParameterRequiredException();

        Member member = memberUseCase.findMemberById(memberId);
        member.clearGithubToken();
    }
}
