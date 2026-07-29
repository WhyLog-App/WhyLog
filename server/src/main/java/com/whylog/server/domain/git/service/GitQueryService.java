package com.whylog.server.domain.git.service;

import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.Repository;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface GitQueryService {

    /**
     * 특정 팀의 레포지토리 목록을 조회합니다.
     */
    List<Repository> getRepositories(Long teamId);

    /**
     * 사용자의 GitHub Access Token 등록 여부를 조회합니다.
     */
    GitResponse.GitHubTokenStatusResponseDTO getGitHubTokenStatus(Long memberId);

    /**
     * 특정 커밋을 조회합니다. (변경된 파일 정보는 GitHub API에서 살시간으로 조회)
     */
    GitResponse.CommitDetailDTO getCommitByHash(Long memberId, Long repositoryId, String hash);

    /**
     * 커서 기반 무한스크롤 - 커밋 목록 조회 (페이지 크기 고정: 10)
     * cursorId가 null이면 첫 페이지, 있으면 다음 페이지
     */
    Slice<Commit> getCommitsByRepository(Long repositoryId, Long cursorId);

    GitResponse.CommitListResponseDTO getCommitListResponse(Long repositoryId, Long cursorId);

}
