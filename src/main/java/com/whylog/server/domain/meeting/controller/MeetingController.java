package com.whylog.server.domain.meeting.controller;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.enums.MeetingStatus;
import com.whylog.server.domain.meeting.exception.MeetingErrorCode;
import com.whylog.server.domain.meeting.service.MeetingCommandService;
import com.whylog.server.domain.meeting.service.MeetingQueryService;
import com.whylog.server.domain.meeting.service.MeetingRtcService;
import com.whylog.server.domain.team.exception.TeamErrorCode;
import com.whylog.server.domain.user.exception.MemberErrorStatus;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExample;
import com.whylog.server.global.apiPayload.annotation.ApiErrorCodeExamples;
import com.whylog.server.global.apiPayload.code.status.ErrorStatus;
import com.whylog.server.global.auth.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "회의 관련 API")
public class MeetingController {

    private final MeetingCommandService meetingCommandService;
    private final MeetingQueryService meetingQueryService;
    private final MeetingRtcService meetingRtcService;

    @GetMapping("/teams/{teamId}/meetings")
    @Operation(summary = "회의 목록 조회 API", description = """
            
            특정 팀의 회의 목록을 조회하는 API입니다.
            status: ONGOING/COMPLETED
            - status는 필수가 아니며, 기본값은 COMPLETED 입니다.
            
            elapse : 경과시간
            - 시:분:초 형태
            - 완료된 회의라면 null로 반환함( 표시할 필요 없으니까 )
            
            페이징 없습니다.
            
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST")
    })
    public ApiResponse<List<MeetingResponse.MeetingListDTO>> getMeetings(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long teamId,
            @RequestParam(required = false, defaultValue = "COMPLETED") MeetingStatus status) {

        return ApiResponse.onSuccess(meetingQueryService.getMeetings(teamId, status));
    }

//    @PostMapping("/meetings/{meetingId}/join")
//    @Operation(summary = "회의 입장 API", description = "특정 회의에 입장하는 API입니다.")
//    public ApiResponse<MeetingResponse.MeetingJoinResponseDTO> joinMeeting(
//            @PathVariable Long meetingId) {
//        return ApiResponse.onSuccess(null);
//    }

    @PostMapping("/teams/{teamId}/meetings")
    @Operation(summary = "회의 생성 API", description = """
            
            새로운 회의를 생성하는 API입니다. 생성하면 실시긴 회의방이 하나 생성됩니다.
            해당 API는 방 생성만 담당합니다. 회의 참여를 위해서는 웹소켓 연결이 필요합니다.
            웹소켓은 JWT 인증, 참여자 상태, WebRTC 시그널링 처리만 담당합니다.
            실제 실시간 음성 전달은 WebRTC/SFU 경로를 사용합니다.
            
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_PARAMETER_REQUIRED"),
            @ApiErrorCodeExample(value = MemberErrorStatus.class, name = "MEMBER_NOT_FOUND"),
            @ApiErrorCodeExample(value = TeamErrorCode.class, name = "TEAM_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.MeetingCreateResponseDTO> createMeeting(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long teamId,
            @Valid @RequestBody MeetingRequest.MeetingCreateDTO request) {
        MeetingResponse.MeetingCreateResponseDTO result = meetingCommandService.makeMeetingRoom(memberId, teamId, request);
        return ApiResponse.onSuccess(result);
    }

    @GetMapping("/meetings/{meetingId}/rtc-token")
    @Operation(summary = "회의 SFU 접속 토큰 발급 API", description = """
            현재 로그인한 사용자가 해당 회의의 LiveKit SFU room에 접속할 수 있도록 join token을 발급합니다.
            프론트는 이 토큰과 serverUrl을 사용해 WebRTC 음성 연결을 수립합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND"),
            @ApiErrorCodeExample(value = MemberErrorStatus.class, name = "MEMBER_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.MeetingRtcTokenDTO> issueRtcToken(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @PathVariable Long meetingId
    ) {
        return ApiResponse.onSuccess(meetingRtcService.issueRtcToken(memberId, meetingId));
    }

    @PatchMapping("/meetings/{meetingId}/end")
    @Operation(summary = "회의 종료 API", description = """
            진행 중인 회의를 종료하는 API입니다.
            회의 종료 시 실시간 회의 참여자들에게 종료를 알리는 웹소켓 메시지를 전송합니다.
            종료 이후의 음성 파일 처리, STT, 회의 분석은 비동기 후처리 파이프라인에서 수행합니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_INVALID_MEMBER"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_ALREADY_ENDED")
    })
    public ApiResponse<MeetingResponse.MeetingEndResponseDTO> endMeeting(
            @Parameter(hidden= true) @CurrentMember Long memberId,
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(meetingCommandService.endMeeting(memberId, meetingId));
    }

    @GetMapping("/meetings/{meetingId}")
    @Operation(summary = "회의 기본 정보 조회 API", description = """
            특정 회의의 기본 정보를 조회하는 API입니다.
            회의명, 날짜, 기간, 참여자 정보를 제공합니다.
            본 API에서 분석결과, 대화기록은 제공하지 않습니다.
            """)
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_PARAMETER_REQUIRED"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.MeetingDetailDTO> getMeetingDetail(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(meetingQueryService.getMeetingDefaultInfo(meetingId));
    }

    @GetMapping("/meetings/{meetingId}/history")
    @Operation(summary = "회의 대화 기록 조회 API", description = "특정 회의의 대화 기록을 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.HistoryListDTO> getHistory(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/analysis")
    @Operation(summary = "회의 분석 결과 조회 API", description = "회의 분석 결과를 조회하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.AnalysisResultDTO> getAnalysisResult(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/audio")
    @Operation(summary = "오디오 리플레이 API", description = "회의 오디오 파일을 리플레이하는 API입니다.")
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_UNAUTHORIZED"),
            @ApiErrorCodeExample(value = ErrorStatus.class, name = "_BAD_REQUEST"),
            @ApiErrorCodeExample(value = MeetingErrorCode.class, name = "MEETING_NOT_FOUND")
    })
    public ApiResponse<MeetingResponse.AudioDTO> getAudio(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

//    @GetMapping("/meetings/{meetingId}/applications")
//    @Operation(summary = "적용사항 목록 조회 API", description = "특정 회의의 적용사항 목록을 조회하는 API입니다.")
//    public ApiResponse<List<MeetingResponse.ApplicationDTO>> getApplications(
//            @PathVariable Long meetingId) {
//        return ApiResponse.onSuccess(null);
//    }
}
