package com.whylog.server.domain.team.controller;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.domain.team.dto.TeamResponse;
import com.whylog.server.domain.team.service.TeamCommandService;
import com.whylog.server.domain.team.service.TeamQueryService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.auth.annotation.CurrentMember;
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
    public ApiResponse<List<DecisionResponse.DecisionListDTO>> getDecisions(
            @PathVariable Long teamId) {
        List<DecisionResponse.DecisionListDTO> decisions = teamQueryService.decisions(teamId);
        return ApiResponse.onSuccess(decisions);
    }

    @PostMapping("/{teamId}/invitations")
    @Operation(summary = "팀 초대 API", description = """
            
            특정 사용자를 팀에 초대합니다. 팀 초대를 하면 상대는 즉시 팀원으로 추가됩니다.

            | 상황 | HTTP Status | Code | Message |
            | --- | --- | --- | --- |
            | 성공 | 200 OK | COMMON200 | 성공입니다. |
            | 이미 팀에 속한 사용자 | 409 Conflict | TEAM_409 | 이미 팀에 속한 사용자입니다. |
            | 팀 없음 | 404 Not Found | TEAM_404 | 존재하지 않는 팀입니다. |
            | 사용자 없음 | 404 Not Found | MEMBER_404 | 찾을 수 없는 유저입니다. |
            
            """)
    public ApiResponse<TeamResponse.InvitationResponseDTO> sendInvitation(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamRequest.InvitationDTO request) {
        TeamResponse.InvitationResponseDTO result = teamCommandService.invite(teamId, request);
        return ApiResponse.onSuccess(result);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 생성 API", description = """
                팀을 생성합니다.
                팀명은 50글자 미만입니다.
                팀 이미지는 선택값입니다.
                팀 생성과 동시에 팀에 참여합니다. ( 따로 호출 X )
            """)
    public ApiResponse<TeamResponse.TeamCreateResponseDTO> createTeam(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestPart("request") TeamRequest.TeamCreateDTO request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(teamCommandService.createTeam(memberId, request, image));
    }

}
