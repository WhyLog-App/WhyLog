package com.whylog.server.domain.decision.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.entity.ApplicationCommits;
import com.whylog.server.domain.decision.entity.Decision;
import com.whylog.server.domain.decision.entity.DecisionCommits;
import com.whylog.server.domain.decision.exception.DecisionErrorCode;
import com.whylog.server.domain.decision.exception.DecisionNotFoundException;
import com.whylog.server.domain.decision.repository.ApplicationCommitsRepository;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.decision.repository.DecisionCommitsRepository;
import com.whylog.server.domain.decision.repository.DecisionRepository;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.Repository;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.exception.RepositoryNotFoundException;
import com.whylog.server.domain.git.repository.CommitConnectionRepository;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.domain.git.repository.RepositoryRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.external.fast.client.FastApiCommitClient;
import com.whylog.server.global.external.fast.client.FastApiSystemClient;
import com.whylog.server.global.external.fast.dto.FastApiResponse;
import com.whylog.server.global.external.fast.dto.request.CommitMatchRequest;
import com.whylog.server.global.external.fast.exception.FastApiErrorCode;
import com.whylog.server.global.external.fast.exception.FastApiException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecisionCommitMatchService {

    private static final int DEFAULT_TOP_K = 5;
    private static final List<String> COMMIT_ID_FIELDS = List.of("commit_id", "commitId");
    private static final List<String> APPLICATION_ID_FIELDS = List.of("application_id", "applicationId");
    private static final List<String> APPLICATION_TITLE_FIELDS = List.of("application_title", "applicationTitle");
    private static final List<String> REPOSITORY_ID_FIELDS = List.of("repository_id", "repositoryId");
    private static final List<String> COMMIT_HASH_FIELDS = List.of("commit_hash", "commitHash");
    private static final List<String> CONFIDENCE_FIELDS = List.of("confidence");
    private static final List<String> REASON_FIELDS = List.of(
            "reason",
            "recommendation_reason",
            "recommendationReason",
            "match_reason",
            "matchReason",
            "explanation"
    );

    private final FastApiCommitClient fastApiCommitClient;
    private final FastApiSystemClient fastApiSystemClient;
    private final DecisionRepository decisionRepository;
    private final ApplicationRepository applicationRepository;
    private final DecisionCommitsRepository decisionCommitsRepository;
    private final ApplicationCommitsRepository applicationCommitsRepository;
    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final CommitConnectionRepository commitConnectionRepository;

    // 결정사항 기준으로 FastAPI 추천 매칭을 요청하고 추천 결과를 저장
    @Transactional
    public JsonNode matchApplicationCommits(Long decisionId) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(DecisionNotFoundException::new);
        Long teamId = decision.getMeeting().getTeam().getId();
        List<Long> repositoryIds = findTeamRepositoryIds(teamId);
        // 외부 연동 장애와 저장 로직 문제를 구분하기 위해 실제 매칭 호출 전에 헬스 체크로 확인
        verifyFastApiAvailable();

        FastApiResponse<JsonNode> response = fastApiCommitClient.matchApplicationCommits(
                new CommitMatchRequest(String.valueOf(decision.getMeeting().getId()), repositoryIds, DEFAULT_TOP_K)
        );

        JsonNode result = requireResult(response);
        return filterAndSaveRecommendations(decision, repositoryIds, result);
    }

    // FastAPI 서버가 매칭 요청을 받을 수 있는 상태인지 확인
    private void verifyFastApiAvailable() {
        FastApiResponse<Map<String, String>> response = fastApiSystemClient.healthCheck();
        if (response == null || Boolean.FALSE.equals(response.isSuccess())) {
            throw new FastApiException(FastApiErrorCode.FAST_API_REQUEST_FAILED);
        }
    }

    // FastAPI 호출 없이 전달받은 응답 JSON을 저장 로직에 태우는 테스트용 메서드
    @Transactional
    public JsonNode saveTestApplicationCommitMatches(Long decisionId, JsonNode fastApiResponse) {
        Decision decision = decisionRepository.findById(decisionId)
                .orElseThrow(DecisionNotFoundException::new);
        Long teamId = decision.getMeeting().getTeam().getId();
        List<Long> repositoryIds = findTeamRepositoryIds(teamId);
        JsonNode result = extractResult(fastApiResponse);

        return filterAndSaveRecommendations(decision, repositoryIds, result);
    }

    // 이미 연결된 커밋을 제외한 뒤 추천 결과 저장
    private JsonNode filterAndSaveRecommendations(Decision decision, List<Long> repositoryIds, JsonNode result) {
        Set<Long> candidateCommitIds = new LinkedHashSet<>();
        collectCommitIds(result, candidateCommitIds);

        if (candidateCommitIds.isEmpty()) {
            saveRecommendations(decision, repositoryIds, result);
            return result;
        }

        Set<Long> connectedCommitIds = new HashSet<>(
                commitConnectionRepository.findConnectedCommitIds(candidateCommitIds.stream().toList())
        );
        if (connectedCommitIds.isEmpty()) {
            saveRecommendations(decision, repositoryIds, result);
            return result;
        }

        JsonNode filteredResult = result.deepCopy();
        // 이미 사용자가 연결한 커밋은 추천과 저장 대상에서 제외
        removeConnectedCommitRecommendations(filteredResult, connectedCommitIds);
        saveRecommendations(decision, repositoryIds, filteredResult);
        return filteredResult;
    }

    // 테스트 API에서 전체 FastAPI 응답이 들어온 경우 result만 꺼냄
    private JsonNode extractResult(JsonNode fastApiResponse) {
        if (fastApiResponse == null || fastApiResponse.isNull()) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }

        JsonNode result = fastApiResponse.get("result");
        if (result != null && !result.isNull()) {
            return result;
        }
        return fastApiResponse;
    }

    // 결정사항이 속한 팀의 레포지토리 ID 목록을 조회
    private List<Long> findTeamRepositoryIds(Long teamId) {
        List<Long> repositoryIds = repositoryRepository.findByTeamId(teamId).stream()
                .map(Repository::getId)
                .toList();
        if (repositoryIds.isEmpty()) {
            throw new RepositoryNotFoundException();
        }
        return repositoryIds;
    }

    // FastAPI 응답 전체에서 commit_id 값을 찾음
    private void collectCommitIds(JsonNode node, Set<Long> commitIds) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            readLong(node, COMMIT_ID_FIELDS).ifPresent(commitIds::add);
            node.fields().forEachRemaining(entry -> collectCommitIds(entry.getValue(), commitIds));
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> collectCommitIds(child, commitIds));
        }
    }

    // 이미 적용사항에 연결된 커밋 추천 항목을 응답 JSON에서 제거
    private void removeConnectedCommitRecommendations(JsonNode node, Set<Long> connectedCommitIds) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            Iterator<JsonNode> iterator = arrayNode.elements();
            while (iterator.hasNext()) {
                JsonNode child = iterator.next();
                Long commitId = readLong(child, COMMIT_ID_FIELDS).orElse(null);
                if (commitId != null && connectedCommitIds.contains(commitId)) {
                    iterator.remove();
                    continue;
                }
                removeConnectedCommitRecommendations(child, connectedCommitIds);
            }
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(entry ->
                    removeConnectedCommitRecommendations(entry.getValue(), connectedCommitIds)
            );
        }
    }

    // 추천 후보를 검증한 뒤 기존 추천 스냅샷을 새 결과로 교체 저장
    private void saveRecommendations(Decision decision, List<Long> repositoryIds, JsonNode result) {
        List<DecisionCommitMatchCandidate> candidates = collectRecommendationCandidates(result);
        Map<DecisionCommitMatchKey, DecisionCommitMatchCandidate> candidatesByKey = candidates.stream()
                .collect(Collectors.toMap(
                        candidate -> new DecisionCommitMatchKey(candidate.applicationUniqueKey(), candidate.commitUniqueKey()),
                        Function.identity(),
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        if (candidatesByKey.isEmpty()) {
            if (hasRecommendedCommits(result)) {
                throw new ErrorHandler(DecisionErrorCode.APPLICATION_NOT_FOUND);
            }
            applicationCommitsRepository.deleteByDecisionId(decision.getId());
            decisionCommitsRepository.deleteByDecisionId(decision.getId());
            decisionRepository.updateReliabilityScore(decision.getId(), null);
            return;
        }

        Map<Long, Application> applicationsById = findAndValidateApplications(decision, candidatesByKey.values());
        Map<Long, Commit> commitsById = findAndValidateCommits(repositoryIds, candidatesByKey.values());

        // 새 추천 스냅샷을 저장하기 전에 기존 추천 결과를 통째로 교체
        applicationCommitsRepository.deleteByDecisionId(decision.getId());
        decisionCommitsRepository.deleteByDecisionId(decision.getId());

        Map<Long, DecisionCommits> decisionCommitsByCommitId = new LinkedHashMap<>();
        for (DecisionCommitMatchCandidate candidate : candidatesByKey.values()) {
            Application application = applicationsById.get(candidate.resolvedApplicationId());
            Commit commit = commitsById.get(candidate.resolvedCommitId());

            DecisionCommits decisionCommits = decisionCommitsByCommitId.computeIfAbsent(
                    candidate.resolvedCommitId(),
                    commitId -> decisionCommitsRepository.save(DecisionCommits.create(decision, commitId))
            );
            applicationCommitsRepository.save(ApplicationCommits.create(
                    application,
                    decisionCommits,
                    candidate.reason(),
                    candidate.confidence()
            ));
        }
        updateReliabilityScore(decision);
    }

    // 저장된 추천 매칭 신뢰도 평균을 결정사항 신뢰도 점수로 갱신
    private void updateReliabilityScore(Decision decision) {
        Double averageConfidence = applicationCommitsRepository.findAverageConfidenceByDecisionId(decision.getId());
        Integer reliabilityScore = averageConfidence != null ? (int) Math.round(averageConfidence) : null;
        decisionRepository.updateReliabilityScore(decision.getId(), reliabilityScore);
    }

    // 추천 후보의 적용사항 ID를 검증하고, 없으면 제목으로 보정
    private Map<Long, Application> findAndValidateApplications(Decision decision,
                                                               java.util.Collection<DecisionCommitMatchCandidate> candidates) {
        List<Application> decisionApplications = applicationRepository.findByDecisionId(decision.getId());
        Map<Long, Application> applicationsById = decisionApplications.stream()
                .collect(Collectors.toMap(Application::getId, Function.identity()));
        Map<String, List<Application>> applicationsByName = decisionApplications.stream()
                .collect(Collectors.groupingBy(Application::getName));

        for (DecisionCommitMatchCandidate candidate : candidates) {
            if (candidate.applicationId() != null) {
                if (!applicationsById.containsKey(candidate.applicationId())) {
                    throw new ErrorHandler(DecisionErrorCode.APPLICATION_NOT_FOUND);
                }
                candidate.resolveApplicationId(candidate.applicationId());
                continue;
            }

            // FastAPI가 application_id를 주지 않는 경우 제목으로 현재 decision의 적용사항을 보정
            if (candidate.applicationTitle() == null || candidate.applicationTitle().isBlank()) {
                throw new ErrorHandler(DecisionErrorCode.APPLICATION_NOT_FOUND);
            }

            List<Application> matchedApplications = applicationsByName.get(candidate.applicationTitle());
            if (matchedApplications == null || matchedApplications.size() != 1) {
                throw new ErrorHandler(DecisionErrorCode.APPLICATION_NOT_FOUND);
            }
            candidate.resolveApplicationId(matchedApplications.get(0).getId());
        }

        return applicationsById;
    }

    // 추천 후보의 커밋 ID를 검증하고, 없으면 repository_id와 hash로 보정
    private Map<Long, Commit> findAndValidateCommits(List<Long> repositoryIds,
                                                     java.util.Collection<DecisionCommitMatchCandidate> candidates) {
        Set<Long> repositoryIdSet = new HashSet<>(repositoryIds);
        Map<Long, Commit> commitsById = new LinkedHashMap<>();

        for (DecisionCommitMatchCandidate candidate : candidates) {
            Commit commit = resolveCommit(candidate, repositoryIdSet);
            candidate.resolveCommitId(commit.getId());
            commitsById.put(commit.getId(), commit);
        }

        return commitsById;
    }

    // 추천 후보 하나를 실제 DB 커밋 엔티티로 해석
    private Commit resolveCommit(DecisionCommitMatchCandidate candidate, Set<Long> repositoryIdSet) {
        if (candidate.commitId() != null) {
            Commit commit = commitRepository.findAllWithRepositoryByIdIn(List.of(candidate.commitId())).stream()
                    .findFirst()
                    .orElseThrow(() -> new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND));
            if (!repositoryIdSet.contains(commit.getRepository().getId())) {
                throw new ErrorHandler(GitErrorCode.REPOSITORY_NOT_FOUND);
            }
            return commit;
        }

        // FastAPI가 commit_id 대신 hash만 주는 경우 repository_id + hash 조합으로 커밋을 찾음
        if (candidate.repositoryId() == null || candidate.commitHash() == null || candidate.commitHash().isBlank()) {
            throw new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND);
        }
        if (!repositoryIdSet.contains(candidate.repositoryId())) {
            throw new ErrorHandler(GitErrorCode.REPOSITORY_NOT_IN_TEAM);
        }

        return commitRepository.findByRepositoryIdAndHash(candidate.repositoryId(), candidate.commitHash())
                .orElseThrow(() -> new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND));
    }

    // FastAPI 응답에서 저장 가능한 추천 후보 목록을 만듦
    private List<DecisionCommitMatchCandidate> collectRecommendationCandidates(JsonNode result) {
        List<DecisionCommitMatchCandidate> candidates = new java.util.ArrayList<>();
        collectRecommendationCandidates(result, null, candidates);
        return candidates;
    }

    // 응답 JSON을 순회하며 적용사항-커밋 추천 후보를 추
    private void collectRecommendationCandidates(JsonNode node, Long applicationId, List<DecisionCommitMatchCandidate> candidates) {
        if (node == null || node.isNull()) {
            return;
        }

        Long currentApplicationId = readLong(node, APPLICATION_ID_FIELDS).orElse(applicationId);
        String currentApplicationTitle = readText(node, APPLICATION_TITLE_FIELDS).orElse(null);
        Long commitId = readLong(node, COMMIT_ID_FIELDS).orElse(null);
        Long repositoryId = readLong(node, REPOSITORY_ID_FIELDS).orElse(null);
        String commitHash = readText(node, COMMIT_HASH_FIELDS).orElse(null);
        Integer confidence = readLong(node, CONFIDENCE_FIELDS)
                .map(Long::intValue)
                .orElse(null);
        if (currentApplicationId != null && commitId != null) {
            candidates.add(new DecisionCommitMatchCandidate(
                    currentApplicationId,
                    currentApplicationTitle,
                    commitId,
                    repositoryId,
                    commitHash,
                    readText(node, REASON_FIELDS).orElse(null),
                    confidence
            ));
        } else if (currentApplicationId != null && commitHash != null) {
            candidates.add(new DecisionCommitMatchCandidate(
                    currentApplicationId,
                    currentApplicationTitle,
                    null,
                    repositoryId,
                    commitHash,
                    readText(node, REASON_FIELDS).orElse(null),
                    confidence
            ));
        } else if (currentApplicationTitle != null && commitHash != null) {
            candidates.add(new DecisionCommitMatchCandidate(
                    null,
                    currentApplicationTitle,
                    null,
                    repositoryId,
                    commitHash,
                    readText(node, REASON_FIELDS).orElse(null),
                    confidence
            ));
        }

        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> collectRecommendationCandidates(entry.getValue(), currentApplicationId, candidates));
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> collectRecommendationCandidates(child, currentApplicationId, candidates));
        }
    }

    // 추천 커밋 배열이 있었는지 확인해 파싱 실패와 추천 없음 상태를 구분
    private boolean hasRecommendedCommits(JsonNode node) {
        if (node == null || node.isNull()) {
            return false;
        }

        if (node.isObject()) {
            JsonNode recommendedCommits = node.get("recommended_commits");
            if (recommendedCommits != null && recommendedCommits.isArray() && !recommendedCommits.isEmpty()) {
                return true;
            }
            Iterator<JsonNode> iterator = node.elements();
            while (iterator.hasNext()) {
                if (hasRecommendedCommits(iterator.next())) {
                    return true;
                }
            }
            return false;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                if (hasRecommendedCommits(child)) {
                    return true;
                }
            }
        }

        return false;
    }

    // 여러 후보 필드명 중 Long으로 읽을 수 있는 값을 찾음
    private Optional<Long> readLong(JsonNode node, List<String> fieldNames) {
        if (node == null || !node.isObject()) {
            return Optional.empty();
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.canConvertToLong()) {
                return Optional.of(value.asLong());
            }
            if (value.isTextual()) {
                try {
                    return Optional.of(Long.valueOf(value.asText()));
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }
        }

        return Optional.empty();
    }

    // 여러 후보 필드명 중 문자열 값을 찾음
    private Optional<String> readText(JsonNode node, List<String> fieldNames) {
        if (node == null || !node.isObject()) {
            return Optional.empty();
        }

        for (String fieldName : fieldNames) {
            JsonNode value = node.get(fieldName);
            if (value != null && value.isValueNode() && !value.asText().isBlank()) {
                return Optional.of(value.asText());
            }
        }

        return Optional.empty();
    }

    // FastAPI 공통 응답에서 result가 비어 있으면 예외처리
    private <T> T requireResult(FastApiResponse<T> response) {
        if (response == null || response.result() == null) {
            throw new FastApiException(FastApiErrorCode.FAST_API_RESPONSE_EMPTY);
        }
        return response.result();
    }
}
