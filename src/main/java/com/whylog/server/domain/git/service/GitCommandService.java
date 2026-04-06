package com.whylog.server.domain.git.service;

import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.entity.Repository;

public interface GitCommandService {

    /**
     * 사용자의 GitHub Access Token을 등록합니다.
     */
    GitResponse.GitHubTokenResponseDTO registerGitHubToken(Long memberId, String accessToken);

    /**
     * 팀에 새로운 레포지토리를 추가합니다.
     */
    Repository createRepository(Long memberId, Long teamId, GitRequest.RepositoryCreateDTO request);

    /**
     * 레포지토리를 동기화합니다.
     */
    GitResponse.RepositorySyncResponseDTO syncRepository(Long memberId, Long repositoryId);
}
