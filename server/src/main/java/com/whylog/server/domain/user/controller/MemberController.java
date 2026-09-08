package com.whylog.server.domain.user.controller;

import com.whylog.server.domain.user.dto.MemberRequest;
import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.exception.MemberErrorCode;
import com.whylog.server.domain.user.service.MemberCommandService;
import com.whylog.server.domain.user.service.MemberQueryService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import com.whylog.server.global.auth.jwt.application.RefreshTokenCookieService;
import com.whylog.server.global.external.s3.S3ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "멤버 관련 API")
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @GetMapping("/me/profile")
    @Operation(summary = "마이페이지 조회 API", description = "현재 로그인한 멤버의 비공개 마이페이지 정보를 조회합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.MyInfoDTO> getMyInfo(
            @Parameter(hidden = true) @CurrentMember Long memberId) {
        return ApiResponse.onSuccess(memberQueryService.getMyInfo(memberId));
    }

    @GetMapping("/{memberId}/profile")
    @Operation(summary = "멤버 공개 프로필 조회 API", description = "다른 사용자가 보는 멤버 공개 프로필을 조회합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.ProfileDTO> getProfile(
            @Parameter(hidden = true) @CurrentMember Long viewerId, @PathVariable Long memberId) {
        return ApiResponse.onSuccess(memberQueryService.getProfile(viewerId, memberId));
    }

    @GetMapping("/{memberId}/projects")
    @Operation(
            summary = "멤버 참여 프로젝트 목록 조회 API",
            description =
                    """

            멤버가 참여 중인 프로젝트를 4개씩 커서 기반으로 조회합니다.
            첫 요청은 cursor 없이 호출하고, 응답의 hasNext가 true이면 nextCursorId를 cursor로 다시 요청합니다.
            내 프로필은 공개범위와 무관하게 조회할 수 있고, 다른 멤버의 비공개 프로필은 MEMBER_NOT_FOUND로 응답합니다.

            """)
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.ParticipatingProjectListResponseDTO> getParticipatingProjects(
            @Parameter(hidden = true) @CurrentMember Long viewerId,
            @PathVariable Long memberId,
            @Parameter(description = "이전 조회의 마지막 프로젝트 ID (첫 요청 시 생략)")
                    @RequestParam(required = false)
                    Long cursor) {
        return ApiResponse.onSuccess(
                memberQueryService.getParticipatingProjects(viewerId, memberId, cursor));
    }

    @GetMapping("/teams")
    @Operation(
            summary = "내 소속 팀 목록 조회 API",
            description =
                    """

            ## 설명
            현재 로그인한 멤버가 소속된 활성 팀 목록을 조회합니다.

            ## 응답
            `team_id`, `name`, `team_image` 필드를 가진 배열을 반환합니다.

            """)
    @ApiErrorCodeExamples({@ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED")})
    public ApiResponse<List<MemberResponse.TeamListResponseDTO>> getMyTeams(
            @Parameter(hidden = true) @CurrentMember Long memberId) {
        return ApiResponse.onSuccess(memberQueryService.getTeams(memberId));
    }

    @PatchMapping("/me/name")
    @Operation(summary = "멤버 이름 변경 API", description = "현재 로그인한 멤버의 표시 이름을 변경합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.MemberUpdateResponseDTO> updateName(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody MemberRequest.NameUpdateDTO request) {
        return ApiResponse.onSuccess(memberCommandService.updateName(memberId, request));
    }

    @PatchMapping("/me/profile/visibility")
    @Operation(summary = "멤버 프로필 공개범위 변경 API", description = "현재 로그인한 멤버의 프로필 공개범위를 변경합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.ProfileVisibilityUpdateResponseDTO> updateProfileVisibility(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody MemberRequest.ProfileVisibilityUpdateDTO request) {
        return ApiResponse.onSuccess(
                memberCommandService.updateProfileVisibility(memberId, request));
    }

    @DeleteMapping("/me/profile-image")
    @Operation(summary = "멤버 프로필 이미지 제거 API", description = "현재 로그인한 멤버의 프로필 이미지를 제거합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MemberResponse.MemberUpdateResponseDTO> removeProfileImage(
            @Parameter(hidden = true) @CurrentMember Long memberId) {
        return ApiResponse.onSuccess(memberCommandService.removeProfileImage(memberId));
    }

    @PostMapping("/me/password/verify")
    @Operation(summary = "현재 비밀번호 검증 API", description = "마이페이지 민감 작업 전 현재 비밀번호를 검증합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_PASSWORD_MISMATCH")
    })
    public ApiResponse<Void> verifyCurrentPassword(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody MemberRequest.CurrentPasswordVerifyDTO request) {
        memberCommandService.verifyCurrentPassword(memberId, request);
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/me/password")
    @Operation(
            summary = "멤버 비밀번호 변경 API",
            description = "현재 비밀번호 확인 후 새 비밀번호로 변경하고 기존 리프레시 세션을 폐기합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_PASSWORD_MISMATCH")
    })
    public ApiResponse<MemberResponse.MemberUpdateResponseDTO> changePassword(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody MemberRequest.PasswordChangeDTO request,
            HttpServletResponse httpServletResponse) {
        MemberResponse.MemberUpdateResponseDTO response =
                memberCommandService.changePassword(memberId, request);
        refreshTokenCookieService.expire(httpServletResponse);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "멤버 프로필 이미지 업로드 API",
            description =
                    """

            ## 설명
            현재 로그인한 멤버의 프로필 이미지를 업로드합니다.

            ## API 호출 방법

            `multipart/form-data`로 요청합니다.
            `image` 파트에 업로드할 이미지 파일을 추가합니다.

            | Part name | Required | Value |
            | --- | --- | --- |
            | `image` | O | 이미지 파일 |

            ### Web FormData 예시

            `Content-Type`은 직접 지정하지 않습니다. 브라우저가 boundary를 포함한 `multipart/form-data` 값을 자동으로 생성해야 합니다.

            """)
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_PROFILE_IMAGE_INVALID"),
        @ApiErrorCodeExample(
                value = MemberErrorCode.class,
                name = "MEMBER_PROFILE_IMAGE_TOO_LARGE"),
        @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_EMPTY"),
        @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_NAME_EMPTY"),
        @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_BUCKET_NOT_CONFIGURED"),
        @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_UPLOAD_FAILED")
    })
    public ApiResponse<MemberResponse.ProfileImageUploadResponseDTO> uploadProfileImage(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @RequestPart("image") MultipartFile image) {
        return ApiResponse.onSuccess(memberCommandService.uploadProfileImage(memberId, image));
    }

    @PostMapping("/me/withdrawal")
    @Operation(summary = "회원 탈퇴 요청 API", description = "현재 회원을 30일 탈퇴 유예 상태로 전환합니다.")
    @ApiErrorCodeExamples({
        @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_NOT_FOUND"),
        @ApiErrorCodeExample(
                value = MemberErrorCode.class,
                name = "MEMBER_WITHDRAWAL_OWNER_HAS_MEMBERS"),
        @ApiErrorCodeExample(value = MemberErrorCode.class, name = "MEMBER_WITHDRAWAL_NOT_ALLOWED")
    })
    public ApiResponse<Void> requestWithdrawal(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            HttpServletResponse httpServletResponse) {
        memberCommandService.requestWithdrawal(memberId);
        refreshTokenCookieService.expire(httpServletResponse);
        return ApiResponse.onSuccess(null);
    }
}
