package com.whylog.server.domain.user.dto;

import com.whylog.server.domain.user.enums.ProfileVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberRequest {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "멤버 이름 변경 요청")
    public static class NameUpdateDTO {

        @Schema(description = "새 이름", example = "홍길동")
        @NotBlank @Size(max = 50) private String name;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "현재 비밀번호 검증 요청")
    public static class CurrentPasswordVerifyDTO {

        @Schema(description = "현재 비밀번호", example = "oldPassword123")
        @NotBlank private String currentPassword;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "비밀번호 변경 요청")
    public static class PasswordChangeDTO {

        @Schema(description = "현재 비밀번호", example = "oldPassword123")
        @NotBlank private String currentPassword;

        @Schema(description = "새 비밀번호", example = "newPassword123")
        @NotBlank @Size(min = 8, max = 100) private String newPassword;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder
    @Schema(description = "프로필 공개범위 변경 요청")
    public static class ProfileVisibilityUpdateDTO {

        @Schema(description = "프로필 공개범위", example = "PUBLIC")
        @NotNull private ProfileVisibility profileVisibility;
    }
}
