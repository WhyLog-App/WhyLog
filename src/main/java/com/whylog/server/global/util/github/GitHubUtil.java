package com.whylog.server.global.util.github;

import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.exception.GitTokenNotRegisteredException;
import com.whylog.server.domain.git.exception.RepositoryNotFoundException;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

@Slf4j
public class GitHubUtil {

    // GitHub URL에서 owner/repo 형식 추출
    public static String extractRepoPath(String repositoryUrl) {
        String[] parts = repositoryUrl.replace("https://github.com/", "")
                .replace(".git", "")
                .split("/");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "/" + parts[parts.length - 1];
        }
        throw new ErrorHandler(GitErrorCode.INVALID_GITHUB_URL);
    }


    // Access Token으로 GitHub 인스턴스 생성
    public static GitHub createGitHubInstance(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new GitTokenNotRegisteredException();
        }
        try {
            return new GitHubBuilder()
                    .withOAuthToken(accessToken)
                    .build();
        } catch (IOException e) {
            log.error("GitHub 인스턴스 생성 실패: {}", e.getMessage());
            throw new GitTokenNotRegisteredException();
        }
    }

    /**
     * GitHub 레포지토리 존재 여부 검증
     */
    public static void validateRepositoryExists(String repositoryUrl, String accessToken) {
        try {
            GitHub gitHub = createGitHubInstance(accessToken);
            String repoPath = extractRepoPath(repositoryUrl);
            gitHub.getRepository(repoPath);
            log.info("레포 검증 성공: {}", repoPath);
        } catch (IOException e) {
            log.error("레포 검증 실패: {}", e.getMessage());
            throw new RepositoryNotFoundException();
        }
    }
}
