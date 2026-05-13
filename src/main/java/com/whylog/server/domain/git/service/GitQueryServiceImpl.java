package com.whylog.server.domain.git.service;
import com.whylog.server.domain.git.repository.CommitConnectionRepository;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.repository.CommitAnalysisRepository;
import com.whylog.server.domain.git.entity.Repository;
import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.exception.RepositoryNotFoundException;
import com.whylog.server.domain.git.exception.GitTokenNotRegisteredException;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.domain.git.repository.RepositoryRepository;
import com.whylog.server.global.util.github.GitHubUtil;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.domain.team.service.TeamUseCase;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHCommit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GitQueryServiceImpl implements GitQueryService {

    private static final int PAGE_SIZE = 10;

    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final CommitAnalysisRepository commitAnalysisRepository;
    private final CommitConnectionRepository commitConnectionRepository;
    private final MemberUseCase memberUseCase;
    private final TeamUseCase teamUseCase;

    /**
     * 특정 팀의 레포지토리 목록을 조회합니다.
     */
    public List<Repository> getRepositories(Long teamId) {

        // null 체크
        if (teamId == null) throw new ParameterRequiredException();

        // 팀 존재 여부 검증
        teamUseCase.findTeamById(teamId);

        return repositoryRepository.findByTeamId(teamId);
    }

    @Override
    public GitResponse.GitHubTokenStatusResponseDTO getGitHubTokenStatus(Long memberId) {
        if (memberId == null) throw new ParameterRequiredException();

        Member member = memberUseCase.findMemberById(memberId);
        return GitResponse.GitHubTokenStatusResponseDTO.from(member.hasGithubToken());
    }

    /**
     * 커서 기반 무한스크롤 - 커밋 목록 조회
     * cursorId가 null이면 첫 페이지, 있으면 다음 페이지
     * 페이지 크기 고정: 10
     */
    public Slice<Commit> getCommitsByRepository(Long repositoryId, Long cursorId) {

        // 레포 확인
        repositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        return commitRepository.findCommitsWithCursor(repositoryId, cursorId, pageable);
    }

    @Override
    public GitResponse.CommitListResponseDTO getCommitListResponse(Long repositoryId, Long cursorId) {
        // 커밋 페이지를 조회한 뒤, 각 커밋에 연결된 적용사항을 함께 응답 DTO로 조립
        Slice<Commit> commitSlice = getCommitsByRepository(repositoryId, cursorId);
        long totalCommitCount = commitRepository.countByRepositoryId(repositoryId);
        long connectedCommitCount = commitConnectionRepository.countByRepositoryId(repositoryId);

        List<Commit> commits = commitSlice.getContent();
        Map<Long, GitResponse.CommitDTO.ConnectedApplicationDTO> connectedApplicationsByCommitId = new LinkedHashMap<>();

        if (!commits.isEmpty()) {
            List<Long> commitIds = commits.stream()
                    .map(Commit::getId)
                    .toList();

            // 현재 페이지의 커밋들에 연결된 적용사항을 한 번에 조회해 commitId 기준으로 매핑
            commitConnectionRepository.findByCommitIds(commitIds).forEach(commitConnection ->
                    connectedApplicationsByCommitId.put(
                            commitConnection.getCommit().getId(),
                            GitResponse.CommitDTO.ConnectedApplicationDTO.from(commitConnection.getApplication())
                    )
            );
        }

        return GitResponse.CommitListResponseDTO.from(
                commitSlice,
                cursorId,
                connectedApplicationsByCommitId,
                totalCommitCount,
                connectedCommitCount
        );
    }

    /**
     * 특정 커밋을 조회합니다. (변경된 파일 정보는 GitHub API를 통해 살시간으로 조회)
     */
    public GitResponse.CommitDetailDTO getCommitByHash(Long memberId, Long repositoryId, String hash) {
        // null 체크
        if (memberId == null || repositoryId == null || hash == null) {
            throw new ParameterRequiredException();
        }

        // DB에서 커밋 정보 조회
        Commit commit = commitRepository.findByRepositoryIdAndHash(repositoryId, hash)
                .orElseThrow(() -> new ErrorHandler(GitErrorCode.COMMIT_NOT_FOUND));
        String summary = commitAnalysisRepository.findByCommitId(commit.getId())
                .map(analysis -> analysis.getSummary())
                .orElse(null);

        // 사용자의 GitHub Token 조회
        Member member = memberUseCase.findMemberById(memberId);
        String accessToken = member.getGithubAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            throw new GitTokenNotRegisteredException();
        }

        // GitHub API에서 변경된 파일 정보 조회
        List<GitResponse.CommitDetailDTO.ChangedFileDTO> changedFiles = List.of();

        try {
            Repository repository = repositoryRepository.findById(repositoryId)
                    .orElseThrow(RepositoryNotFoundException::new);

            GitHub gitHub = GitHubUtil.createGitHubInstance(accessToken);
            String repoPath = GitHubUtil.extractRepoPath(repository.getUrl());
            GHCommit ghCommit = gitHub.getRepository(repoPath).getCommit(hash);

            // 변경된 파일 정보 조회 및 CommitDetailDTO.ChangedFileDTO로 변환
            changedFiles = ghCommit.listFiles().toList().stream()
                    .map(file -> GitResponse.CommitDetailDTO.ChangedFileDTO.builder()
                            .fileName(file.getFileName())
                            .changedCode(file.getPatch() != null ? file.getPatch() : "")
                            .addedLines(file.getLinesAdded())
                            .deletedLines(file.getLinesDeleted())
                            .build())
                    .toList();

            log.info("GitHub API에서 변경된 파일 정보 조회 완료: {} files", changedFiles.size());

        } catch (IOException e) {
            log.warn("GitHub API 호출 실패 (변경 파일 정보): {}", e.getMessage());
            // GitHub API 실패해도 DB의 커밋 정보는 반환 (파일 정보는 빈 리스트)
        }

        // DB에 저장된 정보와 GitHub API에서 조회한 파일 정보를 포함해서 반환
        return GitResponse.CommitDetailDTO.of(commit, summary, changedFiles);
    }
}
