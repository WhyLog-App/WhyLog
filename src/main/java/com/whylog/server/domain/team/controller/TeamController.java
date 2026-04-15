package com.whylog.server.domain.team.controller;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.domain.team.dto.TeamResponse;
import com.whylog.server.domain.team.exception.TeamErrorCode;
import com.whylog.server.domain.team.service.TeamCommandService;
import com.whylog.server.domain.team.service.TeamQueryService;
import com.whylog.server.domain.user.exception.MemberErrorStatus;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import com.whylog.server.global.external.s3.S3ErrorCode;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "팀 관련 API")
public class TeamController {

    private final TeamCommandService teamCommandService;
    private final TeamQueryService teamQueryService;

    @GetMapping("/{teamId}/decisions")
    @Operation(summary = "결정사항 목록 조회 API", description = "특정 팀의 결정사항 목록을 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST")
    })
    public ApiResponse<List<DecisionResponse.DecisionListDTO>> getDecisions(
            @PathVariable Long teamId) {
        List<DecisionResponse.DecisionListDTO> decisions = teamQueryService.decisions(teamId);
        return ApiResponse.onSuccess(decisions);
    }

    @PostMapping("/{teamId}/invitations")
    @Operation(summary = "팀 초대 API", description = """
            특정 사용자를 팀에 초대합니다. 팀 초대를 하면 상대는 즉시 팀원으로 추가됩니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_NOT_FOUND"),
            @ApiErrorCodeExample(value = MemberErrorStatus.class, name = "MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_MEMBER_ALREADY_EXISTS")
    })
    public ApiResponse<TeamResponse.InvitationResponseDTO> sendInvitation(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamRequest.InvitationDTO request) {
        TeamResponse.InvitationResponseDTO result = teamCommandService.invite(teamId, request);
        return ApiResponse.onSuccess(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 생성 API", description = """
            
            ## 설명
            팀을 생성합니다. 해당 api를 호출한 사람은 팀에 참여합니다.
            
            ## API 호출 방법

            `multipart/form-data`로 요청합니다.  
            `request` 파트는 JSON Blob으로 추가하고, `image` 파트는 선택값입니다.

            | Part name | Required | Value |
            | --- | --- | --- |
            | `request` | O | `application/json` 타입의 JSON Blob |
            | `image` | X | 이미지 파일 |

            ### request JSON

            ```json
            {
              "name": "팀명이어떻게다마고치"
            }
            ```

            `Content-Type`은 직접 지정하지 않습니다. 브라우저가 boundary를 포함한 `multipart/form-data` 값을 자동으로 생성해야 합니다.
            
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = TeamRequest.TeamCreateMultipartDTO.class),
                            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_NAME_ALREADY_EXISTS"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_NAME_LENGTH"),
            @ApiErrorCodeExample(value = MemberErrorStatus.class, name = "MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_EMPTY"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_FILE_NAME_EMPTY"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_BUCKET_NOT_CONFIGURED"),
            @ApiErrorCodeExample(value = S3ErrorCode.class, name = "S3_UPLOAD_FAILED")
    })
    public ApiResponse<TeamResponse.TeamCreateResponseDTO> createTeam(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Parameter(hidden = true) @Valid @RequestPart("request") TeamRequest.TeamCreateDTO request,
            @Parameter(hidden = true) @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(teamCommandService.createTeam(memberId, request, image));
    }

}
