package com.whylog.server.domain.git.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

public class GitResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 정보")
    public static class RepositoryDTO {

        @Schema(description = "레포 ID", example = "1")
        private Long repositoryId;

        @Schema(description = "레포 이름", example = "whyLog-BE")
        private String name;

        @Schema(description = "마지막 동기화 시간", example = "2026-03-25T10:30:00")
        private LocalDateTime lastSyncDateTime;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 추가 응답")
    public static class RepositoryCreateResponseDTO {

        @Schema(description = "레포 ID", example = "1")
        private Long repositoryId;

        @Schema(description = "레포 이름", example = "whyLog-BE")
        private String name;

        @Schema(description = "레포 URL", example = "https://github.com/WhyLog-App/WhyLog-BE")
        private String url;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 정보")
    public static class CommitDTO {

        @Schema(description = "커밋 ID", example = "1")
        private Long commitId;

        @Schema(description = "커밋 해시", example = "b8fd9ad")
        private String hash;

        @Schema(description = "커밋 메시지", example = "feat: API 구현")
        private String message;

        @Schema(description = "작성자 이름", example = "김준용")
        private String authorName;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime dateTime;

        @Schema(description = "추가된 줄 수", example = "45")
        private Integer addedLines;

        @Schema(description = "삭제된 줄 수", example = "12")
        private Integer deletedLines;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "커밋 상세 조회 응답")
    public static class CommitDetailDTO {

        @Schema(description = "커밋 ID", example = "1")
        private Long commitId;

        @Schema(description = "커밋 해시", example = "b8fd9ad")
        private String hash;

        @Schema(description = "커밋 메시지", example = "feat: API 구현")
        private String message;

        @Schema(description = "작성자 이름", example = "김준용")
        private String authorName;

        @Schema(description = "작성자 이메일", example = "user@example.com")
        private String authorEmail;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime dateTime;

        @Schema(description = "설명", example = "사용자 마이페이지 조회 및 수정 API를 RESTful 방식으로 구현했습니다.")
        private String description;

        @Schema(description = "변경된 파일 목록")
        private List<ChangedFileDTO> changedFileList;

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "변경된 파일 정보")
        public static class ChangedFileDTO {

            @Schema(description = "파일 이름", example = "src/controllers/userController.js")
            private String fileName;

            @Schema(description = "변경된 코드", example = "+ export const getMyPage = async (req, res) => {")
            private String changedCode;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 동기화 응답")
    public static class RepositorySyncResponseDTO {

        @Schema(description = "레포 ID", example = "1")
        private Long repositoryId;
    }
}
