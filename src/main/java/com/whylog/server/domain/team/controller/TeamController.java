package com.whylog.server.domain.team.controller;

import com.whylog.server.domain.decision.dto.DecisionResponse;
import com.whylog.server.domain.team.dto.TeamRequest;
import com.whylog.server.domain.team.dto.TeamResponse;
import com.whylog.server.domain.team.service.TeamCommandService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.auth.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Team", description = "팀 관련 API")
public class TeamController {

    private final TeamCommandService teamCommandService;

    @GetMapping("/{teamId}/decisions")
    @Operation(summary = "결정사항 목록 조회 API", description = "특정 팀의 결정사항 목록을 조회하는 API입니다.")
    public ApiResponse<DecisionResponse.DecisionListDTO> getDecisions(
            @PathVariable Long teamId) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/{teamId}/invitations")
    @Operation(summary = "팀 초대 API", description = "특정 팀에 사용자를 초대하는 API입니다.")
    public ApiResponse<TeamResponse.InvitationResponseDTO> sendInvitation(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamRequest.InvitationDTO request) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping
    @Operation(summary = "팀 생성 API", description = """
                팀을 생성합니다.
                팀명은 50글자 미만입니다.
                팀 생성과 동시에 팀에 참여합니다. ( 따로 호출 X )
            """)
    public ApiResponse<TeamResponse.TeamCreateResponseDTO> createTeam(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody TeamRequest.TeamCreateDTO request
    ) {
        return ApiResponse.onSuccess(teamCommandService.createTeam(memberId, request));
    }

}
