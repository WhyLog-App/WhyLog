package com.whylog.server.domain.git.service;

import com.whylog.server.domain.git.dto.GitRequest;
import com.whylog.server.domain.git.dto.GitResponse;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.Repository;
import com.whylog.server.domain.git.exception.GitTokenNotRegisteredException;
import com.whylog.server.domain.git.exception.RepositoryNotFoundException;
import com.whylog.server.domain.git.repository.CommitRepository;
import com.whylog.server.domain.git.repository.RepositoryRepository;
import com.whylog.server.global.util.github.GitHubUtil;
import com.whylog.server.domain.team.entity.Team;
import com.whylog.server.domain.team.service.TeamUseCase;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.service.MemberUseCase;
import com.whylog.server.global.apiPayload.exception.ParameterRequiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitCommandServiceImpl implements GitCommandService {

    private final RepositoryRepository repositoryRepository;
    private final CommitRepository commitRepository;
    private final TeamUseCase teamUseCase;
    private final MemberUseCase memberUseCase;

    /**
     * 사용자의 GitHub Access Token을 등록합니다.
     */
    @Override
    @Transactional
    public GitResponse.GitHubTokenResponseDTO registerGitHubToken(Long memberId, String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) throw new ParameterRequiredException();

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
        String accessToken = member.getGithubAccessToken();

        // 레포 URL 유효성 검증 및 GitHub에서 존재 여부 확인
        GitHubUtil.validateRepositoryExists(request.getUrl(), accessToken);

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
        String accessToken = member.getGithubAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            throw new GitTokenNotRegisteredException();
        }

        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(RepositoryNotFoundException::new);

        try {
            GitHub gitHub = GitHubUtil.createGitHubInstance(accessToken);
            String repoPath = GitHubUtil.extractRepoPath(repository.getUrl());
            GHRepository ghRepository = gitHub.getRepository(repoPath);

            // 마지막 동기화 시간 이후의 커밋만 저장
            LocalDateTime lastSyncedAt = repository.getLastSyncedAt();
            syncCommits(ghRepository, repository, lastSyncedAt);

            // 동기화 시간 업데이트
            repository.updateLastSyncedAt(LocalDateTime.now());

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
    private void syncCommits(GHRepository ghRepository, Repository repository, LocalDateTime lastSyncedAt) {
        try {
            var commitQuery = ghRepository.queryCommits();

            // 시간 필터링
            if (lastSyncedAt != null) {
                java.util.Date sinceDate = java.util.Date.from(
                        lastSyncedAt.atZone(ZoneId.systemDefault()).toInstant()
                );
                commitQuery.since(sinceDate); // 이 시간 이후의 커밋만 달라고 요청
            }

            // GitHub에서 가져온 모든 커밋을 리스트로 변환
            List<org.kohsuke.github.GHCommit> allGitHubCommits = commitQuery.list().toList();

            // 모든 커밋의 hash를 추출
            List<String> allHashes = allGitHubCommits.stream()
                    .map(org.kohsuke.github.GHCommit::getSHA1)
                    .toList();

            // DB에 이미 존재하는 hash들을 조회
            Set<String> existingHashes = allHashes.isEmpty()
                    ? java.util.Collections.emptySet()
                    : commitRepository.findExistingHashes(repository.getId(), allHashes);

            //필요한 커밋만 필터링 및 변환
            List<Commit> newCommits = allGitHubCommits.stream()
                    .filter(ghCommit -> !existingHashes.contains(ghCommit.getSHA1()))
                    .map(ghCommit -> {
                        try {
                            // 커밋 정보를 한 번에 조회
                            var shortInfo = ghCommit.getCommitShortInfo();
                            String message = shortInfo.getMessage();

                            // Merge pull request 커밋 제외
                            if (message.startsWith("Merge pull request")) {
                                return null;
                            }

                            // 작성자 프로필 이미지 URL 조회
                            String authorProfileImage = (ghCommit.getAuthor() != null)
                                    ? ghCommit.getAuthor().getAvatarUrl()
                                    : null;

                            return Commit.create(
                                    ghCommit.getSHA1(),
                                    message,
                                    shortInfo.getAuthor().getName(),
                                    shortInfo.getAuthor().getEmail(),
                                    authorProfileImage,
                                    shortInfo.getAuthor().getDate()
                                            .toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDateTime(),
                                    ghCommit.getLinesAdded(),
                                    ghCommit.getLinesDeleted(),
                                    repository
                            );
                        } catch (Exception e) {
                            log.warn("커밋 변환 실패 [{}]: {}", ghCommit.getSHA1(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .toList();

            // db insert
            if (!newCommits.isEmpty()) {
                commitRepository.saveAll(newCommits);
                log.info("새로운 커밋 {}개 동기화 완료: {}", newCommits.size(), ghRepository.getFullName());
            } else {
                log.info("새로 동기화할 커밋이 없습니다: {}", ghRepository.getFullName());
            }

        } catch (IOException e) {
            throw new RepositoryNotFoundException();
        }
    }
}
