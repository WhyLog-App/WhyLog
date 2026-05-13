package com.whylog.server.domain.git.dto;

import com.whylog.server.domain.decision.entity.Application;
import com.whylog.server.domain.git.entity.Commit;
import com.whylog.server.domain.git.entity.Repository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        public static RepositoryDTO from(Repository repository) {
            return RepositoryDTO.builder()
                    .repositoryId(repository.getId())
                    .name(repository.getName())
                    .lastSyncDateTime(repository.getLastSyncedAt())
                    .build();
        }
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

        public static RepositoryCreateResponseDTO from(Repository repository) {
            return RepositoryCreateResponseDTO.builder()
                    .repositoryId(repository.getId())
                    .name(repository.getName())
                    .url(repository.getUrl())
                    .build();
        }
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

        @Schema(description = "작성자 프로필 사진", example = "https://img.com/profile.jpg")
        private String authorProfileImage;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime dateTime;

        @Schema(description = "추가된 줄 수", example = "45")
        private Integer addedLines;

        @Schema(description = "삭제된 줄 수", example = "12")
        private Integer deletedLines;

        @Schema(description = "연결된 적용사항", nullable = true)
        private ConnectedApplicationDTO connectedApplication;

        public static CommitDTO from(Commit commit, ConnectedApplicationDTO connectedApplication) {
            return CommitDTO.builder()
                    .commitId(commit.getId())
                    .hash(commit.getHash())
                    .message(commit.getMessage())
                    .authorName(commit.getAuthorName())
                    .authorProfileImage(commit.getAuthorProfileImage())
                    .dateTime(commit.getDateTime())
                    .addedLines(commit.getAddedLines())
                    .deletedLines(commit.getDeletedLines())
                    .connectedApplication(connectedApplication)
                    .build();
        }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "연결된 적용사항 정보")
        public static class ConnectedApplicationDTO {

            @Schema(description = "적용사항 ID", example = "1")
            private Long applicationId;

            @Schema(description = "적용사항 이름", example = "Redis 기술 변경")
            private String name;

            public static ConnectedApplicationDTO from(Application application) {
                return ConnectedApplicationDTO.builder()
                        .applicationId(application.getId())
                        .name(application.getName())
                        .build();
            }
        }
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

        @Schema(description = "작성자 프로필 사진", example = "https://img.com/profile.jpg")
        private String authorProfileImage;

        @Schema(description = "커밋 날짜", example = "2026-03-24T10:30:00")
        private LocalDateTime dateTime;

        @Schema(description = "설명", example = "사용자 마이페이지 조회 및 수정 API를 RESTful 방식으로 구현했습니다.")
        private String description;

        @Schema(description = "변경된 파일 개수", example = "2")
        private Integer changedFileCount;

        @Schema(description = "변경된 파일 목록")
        private List<ChangedFileDTO> changedFileList;

        public static CommitDetailDTO of(Commit commit, String description, List<ChangedFileDTO> changedFileList) {
            return CommitDetailDTO.builder()
                    .commitId(commit.getId())
                    .hash(commit.getHash())
                    .message(commit.getMessage())
                    .authorName(commit.getAuthorName())
                    .authorEmail(commit.getAuthorEmail())
                    .authorProfileImage(commit.getAuthorProfileImage())
                    .dateTime(commit.getDateTime())
                    .description(description)
                    .changedFileCount(changedFileList.size())
                    .changedFileList(changedFileList)
                    .build();
        }

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

            @Schema(description = "추가된 줄 수", example = "5")
            private Integer addedLines;

            @Schema(description = "삭제된 줄 수", example = "2")
            private Integer deletedLines;
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

        public static RepositorySyncResponseDTO from(Long repositoryId) {
            return RepositorySyncResponseDTO.builder()
                    .repositoryId(repositoryId)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "레포 삭제 응답")
    public static class RepositoryDeleteResponseDTO {

        @Schema(description = "레포 ID", example = "1")
        private Long repositoryId;

        @Schema(description = "삭제 성공 여부", example = "true")
        private Boolean isRemoved;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "깃허브 Access Token 등록 응답")
    public static class GitHubTokenResponseDTO {

        @Schema(description = "GitHub Access Token", example = "ghp_jv******")
        private String accessToken;

        public static GitHubTokenResponseDTO from(String accessToken) {
            return GitHubTokenResponseDTO.builder()
                    .accessToken(accessToken)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "깃허브 Access Token 삭제 응답")
    public static class GitHubTokenDeleteResponseDTO {

        @Schema(description = "삭제 성공 여부", example = "true")
        private Boolean isRemoved;

        public static GitHubTokenDeleteResponseDTO from(Boolean isRemoved) {
            return GitHubTokenDeleteResponseDTO.builder()
                    .isRemoved(isRemoved)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "깃허브 Access Token 등록 여부 응답")
    public static class GitHubTokenStatusResponseDTO {

        @Schema(description = "GitHub Access Token 등록 여부", example = "true")
        private Boolean isRegistered;

        public static GitHubTokenStatusResponseDTO from(Boolean isRegistered) {
            return GitHubTokenStatusResponseDTO.builder()
                    .isRegistered(isRegistered)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "커서 기반 무한스크롤 커밋 목록 응답")
    public static class CommitListResponseDTO {

        @Schema(description = "커밋 목록")
        private List<CommitDTO> commitDTOList;

        @Schema(description = "현재 페이지의 커밋 개수", example = "10")
        private Integer commitListSize;

        @Schema(description = "현재 레포지토리에 있는 전체 커밋 개수", example = "123")
        private Long totalCommitCount;

        @Schema(description = "현재 레포지토리에서 적용사항에 연결된 커밋 개수", example = "80")
        private Long connectedCommitCount;

        @Schema(description = "현재 레포지토리에서 아직 적용사항에 연결되지 않은 커밋 개수", example = "43")
        private Long unconnectedCommitCount;

        @Schema(description = "페이지 처음 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "다음 커서 ID (무한스크롤용)", example = "1")
        private Long nextCursorId;

        public static CommitListResponseDTO from(
                Slice<Commit> commitSlice,
                Long cursorId,
                java.util.Map<Long, CommitDTO.ConnectedApplicationDTO> connectedApplicationsByCommitId,
                Long totalCommitCount,
                Long connectedCommitCount
        ) {
            List<CommitDTO> commitDTOs = commitSlice.getContent().stream()
                    .map(commit -> CommitDTO.from(commit, connectedApplicationsByCommitId.get(commit.getId())))
                    .collect(Collectors.toList());

            // 다음 커서 ID 설정
            Long nextCursorId = commitSlice.hasNext() && !commitDTOs.isEmpty()
                    ? commitDTOs.get(commitDTOs.size() - 1).getCommitId()
                    : null;

            return CommitListResponseDTO.builder()
                    .commitDTOList(commitDTOs)
                    .commitListSize(commitDTOs.size())
                    .totalCommitCount(totalCommitCount)
                    .connectedCommitCount(connectedCommitCount)
                    .unconnectedCommitCount(totalCommitCount - connectedCommitCount)
                    .isFirst(cursorId == null)
                    .hasNext(commitSlice.hasNext())
                    .nextCursorId(nextCursorId)
                    .build();
        }
    }
}
