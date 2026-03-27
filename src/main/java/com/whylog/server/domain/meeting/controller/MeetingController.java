package com.whylog.server.domain.meeting.controller;

import com.whylog.server.domain.meeting.dto.MeetingRequest;
import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/teams/{teamId}/meetings")
    @Operation(summary = "회의 목록 조회 API", description = "특정 팀의 회의 목록을 조회하는 API입니다. (status: ONGOING/COMPLETED)")
    public ApiResponse<List<MeetingResponse.MeetingListDTO>> getMeetings(
            @PathVariable Long teamId,
            @RequestParam(required = false) String status) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/meetings/{meetingId}/join")
    @Operation(summary = "회의 입장 API", description = "특정 회의에 입장하는 API입니다.")
    public ApiResponse<MeetingResponse.MeetingJoinResponseDTO> joinMeeting(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/teams/{teamId}/meetings")
    @Operation(summary = "회의 생성 API", description = "새로운 회의를 생성하는 API입니다.")
    public ApiResponse<MeetingResponse.MeetingCreateResponseDTO> createMeeting(
            @PathVariable Long teamId,
            @Valid @RequestBody MeetingRequest.MeetingCreateDTO request) {
        return ApiResponse.onSuccess(null);
    }

    @PatchMapping("/meetings/{meetingId}/end")
    @Operation(summary = "회의 종료 API", description = "진행 중인 회의를 종료하는 API입니다.")
    public ApiResponse<MeetingResponse.MeetingEndResponseDTO> endMeeting(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}")
    @Operation(summary = "회의 기본 정보 조회 API", description = "특정 회의의 기본 정보를 조회하는 API입니다.")
    public ApiResponse<MeetingResponse.MeetingDetailDTO> getMeetingDetail(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/history")
    @Operation(summary = "회의 대화 기록 조회 API", description = "특정 회의의 대화 기록을 조회하는 API입니다.")
    public ApiResponse<MeetingResponse.HistoryListDTO> getHistory(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/analysis")
    @Operation(summary = "회의 분석 결과 조회 API", description = "회의 분석 결과를 조회하는 API입니다.")
    public ApiResponse<MeetingResponse.AnalysisResultDTO> getAnalysisResult(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/audio")
    @Operation(summary = "오디오 리플레이 API", description = "회의 오디오 파일을 리플레이하는 API입니다.")
    public ApiResponse<MeetingResponse.AudioDTO> getAudio(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/meetings/{meetingId}/applications")
    @Operation(summary = "적용사항 목록 조회 API", description = "특정 회의의 적용사항 목록을 조회하는 API입니다.")
    public ApiResponse<List<MeetingResponse.ApplicationDTO>> getApplications(
            @PathVariable Long meetingId) {
        return ApiResponse.onSuccess(null);
    }
}
