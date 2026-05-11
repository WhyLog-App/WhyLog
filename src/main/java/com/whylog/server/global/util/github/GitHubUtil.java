package com.whylog.server.global.util.github;

import com.whylog.server.domain.git.exception.GitErrorCode;
import com.whylog.server.domain.git.exception.GitTokenNotRegisteredException;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpException;

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
     * (token 유효성은 호출 전에 validateGitHubToken()으로 먼저 확인되어야 함)
     */
    public static void validateRepositoryExists(String repositoryUrl, String accessToken) {
        try {
            GitHub gitHub = createGitHubInstance(accessToken);
            String repoPath = extractRepoPath(repositoryUrl);
            gitHub.getRepository(repoPath);
            log.info("레포 검증 성공: {}", repoPath);
        } catch (HttpException e) {
            if (e.getResponseCode() == 404) {
                log.warn("레포지토리를 찾을 수 없음: {}", repositoryUrl);
                throw new ErrorHandler(GitErrorCode.REPOSITORY_NOT_FOUND);
            }
            log.error("GitHub API 에러 (상태코드: {}): {}", e.getResponseCode(), e.getMessage());
            throw new ErrorHandler(GitErrorCode.GITHUB_API_ERROR);
        } catch (IOException e) {
            log.error("레포 검증 실패: {}", e.getMessage());
            throw new ErrorHandler(GitErrorCode.REPOSITORY_NOT_FOUND);
        }
    }

    /**
     * GitHub Access Token의 유효성을 검증합니다.
     * 사용자 정보 조회를 시도하여 token이 유효한지 확인합니다.
     *
     * @param accessToken 검증할 GitHub access token
     * @throws ErrorHandler token이 유효하지 않으면 GITHUB_TOKEN_INVALID 예외 발생
     */
    public static void validateGitHubToken(String accessToken) {
        try {
            GitHub gitHub = createGitHubInstance(accessToken);
            // 간단한 API 호출로 token 유효성 확인 (사용자 정보 조회)
            gitHub.getMyself();
            log.info("GitHub token 유효성 검증 성공");
        } catch (HttpException e) {
            // 401 Unauthorized: 잘못된 토큰이거나 더 이상 유효하지 않은 토큰
            if (e.getResponseCode() == 401) {
                log.warn("GitHub token 유효하지 않음 (401 Unauthorized)");
                throw new ErrorHandler(GitErrorCode.GITHUB_TOKEN_INVALID);
            }
            // 다른 HTTP 에러
            log.error("GitHub API 에러 (상태코드: {}): {}", e.getResponseCode(), e.getMessage());
            throw new ErrorHandler(GitErrorCode.GITHUB_API_ERROR);
        } catch (IOException e) {
            log.error("GitHub token 검증 중 네트워크 에러: {}", e.getMessage());
            throw new ErrorHandler(GitErrorCode.GITHUB_API_ERROR);
        }
    }

    /**
     * GitHub API 호출 후 HttpException 에러를 처리합니다.
     * (동기화 작업 중 발생하는 에러를 통합 처리)
     *
     * @param e HttpException
     * @throws ErrorHandler 적절한 에러 코드와 함께 예외 발생
     */
    public static void handleHttpException(HttpException e) {
        if (e.getResponseCode() == 401) {
            log.warn("GitHub API 401 Unauthorized - token 만료");
            throw new ErrorHandler(GitErrorCode.GITHUB_TOKEN_EXPIRED);
        }
        // 다른 HTTP 에러
        log.error("GitHub API 에러 (상태코드: {}): {}", e.getResponseCode(), e.getMessage());
        throw new ErrorHandler(GitErrorCode.GITHUB_API_ERROR);
    }
}
