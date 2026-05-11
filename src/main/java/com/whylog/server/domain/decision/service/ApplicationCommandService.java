package com.whylog.server.domain.decision.service;

import com.whylog.server.domain.decision.dto.ApplicationResponse;
import com.whylog.server.domain.decision.dto.DecisionRequest;
import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.decision.exception.DecisionErrorCode;
import com.whylog.server.domain.decision.exception.ApplicationNotFoundException;
import com.whylog.server.domain.decision.repository.ApplicationRepository;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.CommitConnection;
import com.whylog.server.domain.git.entity.CommitConnectionId;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.repository.CommitConnectionRepository;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final CommitRepository commitRepository;
    private final CommitConnectionRepository commitConnectionRepository;

    // 적용사항에 하나 이상의 커밋을 연결합니다.
    @Transactional
    public ApplicationResponse.CommitConnectionResponseDTO connectCommit(Long applicationId,
                                                                         DecisionRequest.CommitConnectionDTO request) {
        // 적용사항 존재 여부 검증
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        // 요청으로 전달된 커밋 ID 목록을 사용
        List<Long> commitIds = request.getCommitIds();

        // 요청된 커밋 ID에 해당하는 커밋을 조회
        Map<Long, Commit> commitsById = commitRepository.findAllById(commitIds).stream()
                .collect(Collectors.toMap(Commit::getId, Function.identity()));

        // 요청한 커밋 중 존재하지 않는 커밋이 있으면 예외 처리
        if (commitsById.size() != commitIds.size()) {
            throw new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND);
        }

        // 요청한 커밋 중 하나라도 이미 연결되어 있으면 전체 요청을 실패 처리
        if (commitIds.stream().anyMatch(commitConnectionRepository::existsByCommitId)) {
            throw new ErrorHandler(DecisionErrorCode.APPLICATION_COMMIT_ALREADY_CONNECTED);
        }

        List<CommitConnectionId> commitConnectionIds = commitIds.stream()
                .map(commitId -> new CommitConnectionId(applicationId, commitId))
                .toList();

        // 신규 커밋 연결 정보를 저장
        List<CommitConnection> commitConnections = commitConnectionIds.stream()
                .map(commitConnectionId -> CommitConnection.create(application, commitsById.get(commitConnectionId.getCommitId())))
                .toList();

        commitConnectionRepository.saveAll(commitConnections);

        return ApplicationResponse.CommitConnectionResponseDTO.builder()
                .applicationId(applicationId)
                .commitIds(commitIds)
                .build();
    }

    // 적용사항에 연결된 커밋 하나를 해제합니다.
    @Transactional
    public ApplicationResponse.CommitConnectionResponseDTO disconnectCommit(Long applicationId,
                                                                            DecisionRequest.CommitDisconnectionDTO request) {
        applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        Long commitId = request.getCommitId();
        CommitConnectionId commitConnectionId = new CommitConnectionId(applicationId, commitId);
        if (!commitConnectionRepository.existsById(commitConnectionId)) {
            throw new ErrorHandler(DecisionErrorCode.APPLICATION_COMMIT_NOT_CONNECTED);
        }

        commitConnectionRepository.deleteById(commitConnectionId);

        return ApplicationResponse.CommitConnectionResponseDTO.builder()
                .applicationId(applicationId)
                .commitIds(List.of(commitId))
                .build();
    }
}
