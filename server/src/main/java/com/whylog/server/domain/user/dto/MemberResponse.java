package com.whylog.server.domain.user.dto;

import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.enums.ProfileView;
import com.whylog.server.domain.user.enums.ProfileVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

public class MemberResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "마이페이지 응답")
    public static class MyInfoDTO {

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "member@example.com")
        private String email;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.whylog.com/members/profile.png")
        private String profileImage;

        @Schema(description = "참여 중인 프로젝트 수", example = "2")
        private Long participatingProjectCount;

        @Schema(description = "계정 상태", example = "ACTIVE")
        private AccountStatus accountStatus;

        @Schema(description = "프로필 공개범위", example = "PUBLIC")
        private ProfileVisibility profileVisibility;

        @Schema(description = "최근 완료 회의 목록")
        private List<RecentMeetingDTO> recentMeetings;

        @Schema(description = "최근 결정 목록")
        private List<RecentDecisionDTO> recentDecisions;
    }

    @Schema(description = "멤버 공개 프로필 응답")
    public interface ProfileDTO {

        ProfileView getProfileView();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "전체 멤버 공개 프로필 응답")
    public static class FullProfileDTO implements ProfileDTO {

        @Builder.Default
        @Schema(description = "프로필 응답 뷰", example = "FULL")
        private ProfileView profileView = ProfileView.FULL;

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "member@example.com")
        private String email;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.whylog.com/members/profile.png")
        private String profileImage;

        @Schema(description = "참여 중인 프로젝트 수", example = "2")
        private Long participatingProjectCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "비공개 멤버 프로필 응답")
    public static class PrivateProfileDTO implements ProfileDTO {

        @Builder.Default
        @Schema(description = "프로필 응답 뷰", example = "PRIVATE")
        private ProfileView profileView = ProfileView.PRIVATE;

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "member@example.com")
        private String email;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.whylog.com/members/profile.png")
        private String profileImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "탈퇴한 멤버 공개 프로필 응답")
    public static class WithdrawnProfileDTO implements ProfileDTO {

        @Builder.Default
        @Schema(description = "프로필 응답 뷰", example = "WITHDRAWN")
        private ProfileView profileView = ProfileView.WITHDRAWN;

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "표시 이름", example = "탈퇴한 사용자")
        private String name;

        @Schema(description = "삭제된 이메일 자리값", example = "")
        private String email;

        @Schema(description = "삭제된 프로필 이미지 URL", nullable = true)
        private String profileImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "참여 중인 프로젝트 커서 페이지 응답")
    public static class ParticipatingProjectListResponseDTO {

        @Schema(description = "참여 프로젝트 목록")
        private List<ParticipatingProjectSummaryDTO> participatingProjects;

        @Schema(description = "현재 페이지의 프로젝트 개수", example = "4")
        private Integer projectListSize;

        @Schema(description = "페이지 처음 여부", example = "true")
        private Boolean isFirst;

        @Schema(description = "다음 페이지가 있는지 여부", example = "true")
        private Boolean hasNext;

        @Schema(description = "다음 요청에 사용할 커서 프로젝트 ID", example = "11")
        private Long nextCursorId;

        public static ParticipatingProjectListResponseDTO from(
                Slice<ParticipatingProjectSummaryDTO> participatingProjectSlice, Long cursorId) {
            List<ParticipatingProjectSummaryDTO> participatingProjects =
                    participatingProjectSlice.getContent();
            Long nextCursorId =
                    participatingProjectSlice.hasNext() && !participatingProjects.isEmpty()
                            ? participatingProjects
                                    .get(participatingProjects.size() - 1)
                                    .getProjectId()
                            : null;

            return new ParticipatingProjectListResponseDTO(
                    participatingProjects,
                    participatingProjects.size(),
                    cursorId == null,
                    participatingProjectSlice.hasNext(),
                    nextCursorId);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "참여 중인 프로젝트 요약")
    public static class ParticipatingProjectSummaryDTO {

        @Schema(description = "프로젝트 ID", example = "1")
        private Long projectId;

        @Schema(description = "프로젝트명", example = "WhyLog")
        private String name;

        @Schema(
                description = "프로젝트 이미지 URL",
                example = "https://cdn.whylog.com/teams/team-image.png")
        private String image;

        @Schema(description = "멤버가 참여한 완료 회의 수", example = "12")
        private Long memberCompletedMeetingCount;

        @Schema(description = "멤버가 참여한 완료 회의 누적 시간(초)", example = "3600")
        private Long memberCompletedMeetingDurationSeconds;

        @Schema(description = "프로젝트 저장 커밋 수", example = "120")
        private Long projectStoredCommitCount;

        @Schema(description = "프로젝트 저장소 중 가장 최근 동기화 시각")
        private LocalDateTime lastSyncedAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "최근 완료 회의 요약")
    public static class RecentMeetingDTO {

        @Schema(description = "회의 ID", example = "1")
        private Long meetingId;

        @Schema(description = "프로젝트 ID", example = "1")
        private Long projectId;

        @Schema(description = "프로젝트명", example = "WhyLog")
        private String projectName;

        @Schema(description = "회의명", example = "스프린트 회의")
        private String name;

        @Schema(description = "회의 종료 시각")
        private LocalDateTime endedAt;

        @Schema(description = "회의 시간(초)", example = "1800")
        private Long durationSeconds;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버가 참여한 회의의 최근 결정 요약")
    public static class RecentDecisionDTO {

        @Schema(description = "결정 ID", example = "1")
        private Long decisionId;

        @Schema(description = "프로젝트 ID", example = "1")
        private Long projectId;

        @Schema(description = "프로젝트명", example = "WhyLog")
        private String projectName;

        @Schema(description = "결정의 기준 회의명", example = "스프린트 회의")
        private String name;

        @Schema(description = "결정 생성 시각")
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버 계정 정보 변경 응답")
    public static class MemberUpdateResponseDTO {

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "이름", example = "홍길동")
        private String name;

        @Schema(description = "이메일", example = "member@example.com")
        private String email;

        @Schema(description = "프로필 이미지 URL", example = "https://cdn.whylog.com/members/profile.png")
        private String profileImage;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버 프로필 공개범위 변경 응답")
    public static class ProfileVisibilityUpdateResponseDTO {

        @Schema(description = "프로필 공개범위", example = "PUBLIC")
        private ProfileVisibility profileVisibility;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버 프로필 이미지 업로드 응답")
    public static class ProfileImageUploadResponseDTO {

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(
                description = "프로필 이미지 URL",
                example =
                        "https://server-images-437659978683-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/member_profile/member_profile_image_2026-04-15-03-23-22-262.png")
        private String profileImageUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "소속 팀 목록 조회 응답")
    public static class TeamListResponseDTO {

        @Schema(description = "팀 ID", example = "1")
        private Long teamId;

        @Schema(description = "팀명", example = "팀명이 어떻게 다마고치")
        private String name;

        @Schema(description = "팀 이미지 URL", example = "https://cdn.whylog.com/teams/team-image.png")
        private String teamImage;
    }
}
