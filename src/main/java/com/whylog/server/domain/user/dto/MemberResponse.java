package com.whylog.server.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버 프로필 이미지 업로드 응답")
    public static class ProfileImageUploadResponseDTO {

        @Schema(description = "멤버 ID", example = "1")
        private Long memberId;

        @Schema(description = "프로필 이미지 URL", example = "https://server-images-437659978683-ap-northeast-2-an.s3.ap-northeast-2.amazonaws.com/member_profile/member_profile_image_2026-04-15-03-23-22-262.png")
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
