package com.whylog.server.domain.user.controller;

import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.exception.MemberErrorStatus;
import com.whylog.server.domain.user.service.MemberCommandService;
import com.whylog.server.domain.user.service.MemberQueryService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import com.whylog.server.global.external.s3.S3ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/teams")
    @Operation(summary = "내 소속 팀 목록 조회 API", description = """
            
            ## 설명
            현재 로그인한 멤버가 소속된 활성 팀 목록을 조회합니다.
            
            ## 응답
            `team_id`, `name`, `team_image` 필드를 가진 배열을 반환합니다.
            
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED")
    })
    public ApiResponse<List<MemberResponse.TeamListResponseDTO>> getMyTeams(
            @Parameter(hidden = true) @CurrentMember Long memberId
    ) {
        return ApiResponse.onSuccess(memberQueryService.getTeams(memberId));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "멤버 프로필 이미지 업로드 API", description = """
            
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
            @ApiErrorCodeExample(value = MemberErrorStatus.class, name = "MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_EMPTY"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_NAME_EMPTY"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_BUCKET_NOT_CONFIGURED"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_UPLOAD_FAILED")
    })
    public ApiResponse<MemberResponse.ProfileImageUploadResponseDTO> uploadProfileImage(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.onSuccess(memberCommandService.uploadProfileImage(memberId, image));
    }

}
