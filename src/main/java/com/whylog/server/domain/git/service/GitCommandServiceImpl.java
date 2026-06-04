package com.whylog.server.domain.git.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.decision.repository.ApplicationCommitsRepository;
import com.whylog.server.domain.decision.repository.DecisionCommitsRepository;
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
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.HttpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitCommandServiceImpl implements GitCommandService {

    private static final int MAX_COMMIT_ANALYZE_RETRY_ATTEMPTS = 3;
    private static final long COMMIT_ANALYZE_FIRST_RETRY_INTERVAL_MILLIS = 30000L;
    private static final long COMMIT_ANALYZE_SECOND_RETRY_INTERVAL_MILLIS = 60000L;

    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final CommitAnalysisRepository commitAnalysisRepository;
    private final DecisionCommitsRepository decisionCommitsRepository;
    private final ApplicationCommitsRepository applicationCommitsRepository;
    private final FastApiCommitClient fastApiCommitClient;
    private final TeamUseCase teamUseCase;
    private final MemberUseCase memberUseCase;
    private final ObjectMapper objectMapper;

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

    @Override
    @Transactional
    public GitResponse.GitHubTokenDeleteResponseDTO deleteGitHubToken(Long memberId) {
        if (memberId == null) throw new ParameterRequiredException();

        Member member = memberUseCase.findMemberById(memberId);
        member.clearGithubToken();

        return GitResponse.GitHubTokenDeleteResponseDTO.from(true);
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
            if (e.getCode() == GitErrorCode.GITHUB_TOKEN_EXPIRED
                    || e.getCode() == GitErrorCode.GITHUB_TOKEN_INVALID) {
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

    @Override
    @Transactional
    public GitResponse.RepositoryDeleteResponseDTO deleteRepository(Long repositoryId) {
        if (repositoryId == null) throw new ParameterRequiredException();

        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        List<Long> commitIds = commitRepository.findIdsByRepositoryId(repositoryId);

        if (!commitIds.isEmpty()) {
            List<Long> decisionCommitIds = decisionCommitsRepository.findIdsByCommitIdIn(commitIds);
            if (!decisionCommitIds.isEmpty()) {
                applicationCommitsRepository.deleteByDecisionCommitsIdIn(decisionCommitIds);
                decisionCommitsRepository.deleteByCommitIdIn(commitIds);
            }
        }

        repositoryRepository.delete(repository);

        return GitResponse.RepositoryDeleteResponseDTO.builder()
                .repositoryId(repositoryId)
                .isRemoved(true)
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
                .distinct()
                .toList();

        Set<String> existingHashes = allHashes.isEmpty()
                ? Collections.emptySet()
                : commitRepository.findExistingHashes(repository.getId(), allHashes);

        Set<String> syncTargetHashes = new HashSet<>();
        List<Commit> newCommits = allGitHubCommits.stream()
                .filter(ghCommit -> !existingHashes.contains(ghCommit.getSHA1()))
                .filter(ghCommit -> syncTargetHashes.add(ghCommit.getSHA1()))
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

        List<Commit> savedCommits = new ArrayList<>();
        if (!newCommits.isEmpty()) {
            savedCommits = commitRepository.saveAllAndFlush(newCommits);
            log.info("새로운 커밋 {}개 동기화 완료: {}", savedCommits.size(), ghRepository.getFullName());
        } else {
            log.info("새로 동기화할 커밋이 없습니다: {}", ghRepository.getFullName());
        }

        List<Commit> embeddingNotReadyCommits = commitRepository.findEmbeddingNotReadyCommitsByRepositoryId(repository.getId());
        List<Commit> analyzeTargets = mergeAnalyzeTargets(savedCommits, embeddingNotReadyCommits);

        if (!analyzeTargets.isEmpty()) {
            triggerCommitAnalyzeRunsAfterCommit(ghRepository, repository, analyzeTargets);
            log.info("커밋 분석 요청 대상 {}개 확인: {}", analyzeTargets.size(), ghRepository.getFullName());
        }
    }

    private List<Commit> mergeAnalyzeTargets(List<Commit> savedCommits, List<Commit> embeddingNotReadyCommits) {
        Map<Long, Commit> analyzeTargets = new LinkedHashMap<>();
        savedCommits.forEach(commit -> analyzeTargets.put(commit.getId(), commit));
        embeddingNotReadyCommits.forEach(commit -> analyzeTargets.putIfAbsent(commit.getId(), commit));
        return new ArrayList<>(analyzeTargets.values());
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
        CommitAnalyzeRequest request = null;
        try {
            GHCommit ghCommit = ghRepository.getCommit(commit.getHash());
            List<ChangedFile> changedFiles = ghCommit.listFiles().toList().stream()
                    .map(file -> toChangedFile(ghRepository, ghCommit, file))
                    .filter(Objects::nonNull)
                    .toList();

            if (changedFiles.isEmpty()) {
                log.info("분석 가능한 변경 파일이 없어 커밋 분석을 건너뜁니다: commitHash={}", commit.getHash());
                return;
            }

            request = new CommitAnalyzeRequest(
                    null,
                    commit.getHash(),
                    repository.getId().intValue(),
                    commit.getMessage(),
                    changedFiles
            );

            for (int attempt = 1; attempt <= MAX_COMMIT_ANALYZE_RETRY_ATTEMPTS; attempt++) {
                try {
                    FastApiResponse<JsonNode> createResponse = fastApiCommitClient.analyzeCommit(request);
                    JsonNode createResult = requireResult(createResponse);
                    String runId = readText(createResult, "run_id");
                    if (runId == null || runId.isBlank()) {
                        throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
                    }

                    JsonNode runResult = pollCommitAnalyzeRun(runId);
                    saveCommitAnalysis(commit, runResult);
                    return;
                } catch (FastApiException e) {
                    if (!isRetryableCommitAnalyzeFailure(e) || attempt == MAX_COMMIT_ANALYZE_RETRY_ATTEMPTS) {
                        throw e;
                    }

                    long retryIntervalMillis = resolveCommitAnalyzeRetryIntervalMillis(attempt);
                    log.warn("커밋 분석 재시도: commitHash={}, attempt={}/{}",
                            commit.getHash(), attempt + 1, MAX_COMMIT_ANALYZE_RETRY_ATTEMPTS);
                    sleep(retryIntervalMillis);
                }
            }
        } catch (Exception e) {
            logCommitAnalyzeRequestOnClientError(commit, request, e);
            log.warn("커밋 분석 run 생성 실패: commitHash={}", commit.getHash(), e);
        }
    }

    /**
     * FastAPI 분석 run 상태를 폴링해 summary와 embedding이 준비된 최종 결과를 가져옵니다.
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
            String error = readText(runResult, "error");
            String summary = readNestedText(runResult, "result", "summary");
            boolean embeddingReady = isCommitEmbeddingReady(runResult);

            if (isFailed(status, phase)) {
                throw new FastApiException(
                        FastApiErrorCode.FAST_API_REQUEST_FAILED,
                        new IllegalStateException(error != null ? error : "commit analyze run failed")
                );
            }

            if (summary != null && !summary.isBlank() && embeddingReady) {
                return runResult;
            }

            sleep(3000L);
        }

        throw new FastApiException(
                FastApiErrorCode.FAST_API_REQUEST_FAILED,
                new IllegalStateException("commit analyze run poll timeout")
        );
    }

    /**
     * 완료된 분석 결과에서 summary와 embedding 준비 상태를 upsert 저장합니다.
     */
    private void saveCommitAnalysis(Commit commit, JsonNode runResult) {
        String summary = readNestedText(runResult, "result", "summary");
        boolean embeddingReady = isCommitEmbeddingReady(runResult);

        if (summary == null || summary.isBlank() || !embeddingReady) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        CommitAnalysis commitAnalysis = commitAnalysisRepository.findByCommitId(commit.getId())
                .orElseGet(() -> CommitAnalysis.create(commit));
        commitAnalysis.updateSummary(summary, embeddingReady);
        commitAnalysisRepository.save(commitAnalysis);

        log.info("커밋 분석 저장 완료: commitHash={}, embeddingReady={}", commit.getHash(), embeddingReady);
    }

    /**
     * FastAPI run 상태가 실패인지 확인합니다.
     */
    private boolean isFailed(String status, String phase) {
        return "failed".equalsIgnoreCase(status) || "failed".equalsIgnoreCase(phase);
    }

    /**
     * FastAPI 커밋 분석 결과의 embedding 준비 여부를 확인합니다.
     */
    private boolean isCommitEmbeddingReady(JsonNode runResult) {
        String phase = readText(runResult, "phase");
        if ("embedding_ready".equalsIgnoreCase(phase)) {
            return true;
        }

        Boolean embeddingReady = readBoolean(runResult, "embedding_ready");
        if (embeddingReady != null) {
            return embeddingReady;
        }

        Boolean camelEmbeddingReady = readBoolean(runResult, "embeddingReady");
        if (camelEmbeddingReady != null) {
            return camelEmbeddingReady;
        }

        Boolean nestedEmbeddingReady = readNestedBoolean(runResult, "result", "embedding_ready");
        if (nestedEmbeddingReady != null) {
            return nestedEmbeddingReady;
        }

        Boolean nestedCamelEmbeddingReady = readNestedBoolean(runResult, "result", "embeddingReady");
        return Boolean.TRUE.equals(nestedCamelEmbeddingReady);
    }

    /**
     * 커밋 분석 실패가 재시도 가능한 일시적 오류인지 확인합니다.
     */
    private boolean isRetryableCommitAnalyzeFailure(FastApiException exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof RestClientResponseException restClientResponseException) {
                return restClientResponseException.getStatusCode().is5xxServerError();
            }

            String message = cause.getMessage();
            if (message != null) {
                if (message.contains("Gemini 응답 시간이 초과되었습니다.")
                        || message.contains("504")
                        || message.contains("poll timeout")) {
                    return true;
                }
            }

            cause = cause.getCause();
        }
        return false;
    }

    /**
     * 요청 본문 검증 실패가 발생한 경우 FastAPI로 전달한 요청 JSON을 함께 기록합니다.
     */
    private void logCommitAnalyzeRequestOnClientError(Commit commit, CommitAnalyzeRequest request, Exception exception) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof RestClientResponseException restClientResponseException
                    && restClientResponseException.getStatusCode().is4xxClientError()) {
                try {
                    log.warn("커밋 분석 요청 본문: commitHash={}, payload={}",
                            commit.getHash(),
                            objectMapper.writeValueAsString(request));
                } catch (JsonProcessingException jsonProcessingException) {
                    log.warn("커밋 분석 요청 본문 직렬화 실패: commitHash={}", commit.getHash(), jsonProcessingException);
                }
                return;
            }
            cause = cause.getCause();
        }
    }

    /**
     * 커밋 분석 재시도 순서에 맞는 대기 시간을 반환합니다.
     */
    private long resolveCommitAnalyzeRetryIntervalMillis(int attempt) {
        if (attempt == 1) {
            return COMMIT_ANALYZE_FIRST_RETRY_INTERVAL_MILLIS;
        }
        return COMMIT_ANALYZE_SECOND_RETRY_INTERVAL_MILLIS;
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
     * JSON 필드의 Boolean 값을 안전하게 읽습니다.
     */
    private Boolean readBoolean(JsonNode node, String fieldName) {
        JsonNode value = node != null ? node.get(fieldName) : null;
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isTextual()) {
            String text = value.asText();
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return null;
    }

    /**
     * 중첩 JSON 필드의 Boolean 값을 안전하게 읽습니다.
     */
    private Boolean readNestedBoolean(JsonNode node, String parentFieldName, String childFieldName) {
        JsonNode parent = node != null ? node.get(parentFieldName) : null;
        return readBoolean(parent, childFieldName);
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
    private ChangedFile toChangedFile(GHRepository ghRepository, GHCommit ghCommit, GHCommit.File file) {
        String patch = file.getPatch();
        if (patch != null && !patch.isBlank()) {
            return new ChangedFile(file.getFileName(), patch);
        }

        try {
            if (isBinaryFile(file)) {
                return null;
            }

            String fallbackPatch = buildFallbackPatch(ghRepository, ghCommit, file);
            if (fallbackPatch == null || fallbackPatch.isBlank()) {
                return null;
            }

            return new ChangedFile(file.getFileName(), fallbackPatch);
        } catch (IOException e) {
            log.warn("변경 파일 patch 생성 실패: fileName={}, message={}", file.getFileName(), e.getMessage());
            return null;
        }
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
     * patch 가 없는 파일에 대해 before/after 내용을 다시 조회하여 fallback diff 를 생성합니다.
     */
    private String buildFallbackPatch(GHRepository ghRepository, GHCommit ghCommit, GHCommit.File file) throws IOException {
        String beforePath = file.getPreviousFilename() != null ? file.getPreviousFilename() : file.getFileName();
        String afterPath = file.getFileName();
        String status = file.getStatus();

        String beforeContent = null;
        String afterContent = null;

        if (!"added".equalsIgnoreCase(status)) {
            beforeContent = readFileContentIfExists(ghRepository, beforePath, resolveParentSha(ghCommit));
        }
        if (!"removed".equalsIgnoreCase(status)) {
            afterContent = readFileContentIfExists(ghRepository, afterPath, resolveCurrentSha(ghCommit));
        }

        if (beforeContent == null && afterContent == null) {
            return null;
        }

        return buildUnifiedDiff(beforePath, afterPath, beforeContent, afterContent);
    }

    /**
     * 현재 파일이 바이너리 파일인지 추정합니다.
     */
    private boolean isBinaryFile(GHCommit.File file) {
        String fileName = file.getFileName().toLowerCase();
        return fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".webp")
                || fileName.endsWith(".svg")
                || fileName.endsWith(".pdf")
                || fileName.endsWith(".zip")
                || fileName.endsWith(".jar")
                || fileName.endsWith(".war")
                || fileName.endsWith(".class")
                || fileName.endsWith(".mp3")
                || fileName.endsWith(".mp4")
                || fileName.endsWith(".mov")
                || fileName.endsWith(".avi")
                || fileName.endsWith(".wav")
                || fileName.endsWith(".ttf")
                || fileName.endsWith(".otf")
                || fileName.endsWith(".woff")
                || fileName.endsWith(".woff2")
                || fileName.endsWith(".ico");
    }

    /**
     * 특정 ref 기준 파일 내용을 조회하고 없으면 null 을 반환합니다.
     */
    private String readFileContentIfExists(GHRepository ghRepository, String path, String ref) throws IOException {
        if (path == null || ref == null || ref.isBlank()) {
            return null;
        }

        try {
            GHContent content = ghRepository.getFileContent(path, ref);
            try (var inputStream = content.read()) {
                byte[] bytes = inputStream.readAllBytes();
                if (containsNullByte(bytes)) {
                    return null;
                }
                return new String(bytes);
            }
        } catch (HttpException e) {
            return null;
        }
    }

    /**
     * fallback diff 생성을 위해 현재 커밋 sha 를 반환합니다.
     */
    private String resolveCurrentSha(GHCommit ghCommit) {
        return ghCommit.getSHA1();
    }

    /**
     * fallback diff 생성을 위해 부모 커밋 sha 를 반환합니다.
     */
    private String resolveParentSha(GHCommit ghCommit) {
        List<String> parentSha1s = ghCommit.getParentSHA1s();
        if (parentSha1s == null || parentSha1s.isEmpty()) {
            return null;
        }
        return parentSha1s.get(0);
    }

    /**
     * before/after 전체 내용을 기반으로 단순 unified diff 문자열을 생성합니다.
     */
    private String buildUnifiedDiff(String beforePath, String afterPath, String beforeContent, String afterContent) {
        List<String> beforeLines = beforeContent == null ? List.of() : Arrays.asList(beforeContent.split("\\R", -1));
        List<String> afterLines = afterContent == null ? List.of() : Arrays.asList(afterContent.split("\\R", -1));

        if (beforeLines.equals(afterLines)) {
            return null;
        }

        StringBuilder diff = new StringBuilder();
        diff.append("--- a/").append(beforePath).append("\n");
        diff.append("+++ b/").append(afterPath).append("\n");
        diff.append("@@ -1,").append(beforeLines.size()).append(" +1,").append(afterLines.size()).append(" @@\n");

        for (String line : beforeLines) {
            diff.append("-").append(line).append("\n");
        }
        for (String line : afterLines) {
            diff.append("+").append(line).append("\n");
        }
        return diff.toString();
    }

    /**
     * 파일 바이트 배열에 null byte 가 포함되어 있으면 바이너리로 간주합니다.
     */
    private boolean containsNullByte(byte[] bytes) {
        for (byte value : bytes) {
            if (value == 0) {
                return true;
            }
        }
        return false;
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
