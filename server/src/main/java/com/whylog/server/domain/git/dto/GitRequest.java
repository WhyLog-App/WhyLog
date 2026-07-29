package com.whylog.server.domain.git.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

public class GitRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 추가 요청")
    public static class RepositoryCreateDTO {

        @Schema(description = "레포 이름", example = "whyLog-BE")
        @NotBlank
        private String name;

        @Schema(description = "레포 URL", example = "https://github.com/WhyLog-App/WhyLog-BE")
        @NotBlank @URL
        private String url;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "GitHub Access Token 등록 요청")
    public static class GitHubTokenDTO {

        @Schema(description = "GitHub Access Token", example = "ghp_jv******")
        @NotBlank
        @JsonProperty("access_token")
        private String accessToken;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 생성 DTO")
    public static class CommitCreateDTO {

        @Schema(description = "커밋 해시", example = "abc123def456")
        private String hash;

        @Schema(description = "커밋 메시지", example = "[feat] 사용자 인증 기능 추가")
        private String message;

        @Schema(description = "작성자 이름", example = "김준용")
        private String authorName;

        @Schema(description = "작성자 이메일", example = "user@example.com")
        private String authorEmail;

        @Schema(description = "작성자 프로필 이미지", example = "https://avatars.githubusercontent.com/u/123?v=4")
        private String authorProfileImage;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime dateTime;

        @Schema(description = "추가된 줄 수", example = "45")
        private Integer addedLines;

        @Schema(description = "삭제된 줄 수", example = "12")
        private Integer deletedLines;
    }
}

