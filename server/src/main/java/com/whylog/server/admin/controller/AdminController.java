package com.whylog.server.admin.controller;

import com.whylog.server.admin.dto.AdminResponse;
import com.whylog.server.admin.service.AdminMeetingRoomService;
import com.whylog.server.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "관리자 기능")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminMeetingRoomService adminMeetingRoomService;

    @GetMapping("/livekit/rooms")
    @Operation(summary = "LiveKit 열린 room 목록 조회", description = "LiveKit에 현재 열려 있는 room 목록을 조회합니다.")
    public ApiResponse<AdminResponse.LiveKitRoomListDTO> listLiveKitRooms() {
        return ApiResponse.onSuccess(adminMeetingRoomService.listLiveKitRooms());
    }

    @DeleteMapping("/livekit/rooms/{roomName}")
    @Operation(summary = "LiveKit room 삭제", description = "지정한 LiveKit room을 삭제하고 연결된 참여자를 모두 종료합니다.")
    public ApiResponse<AdminResponse.LiveKitRoomDeleteDTO> deleteLiveKitRoom(
            @PathVariable String roomName
    ) {
        return ApiResponse.onSuccess(adminMeetingRoomService.deleteLiveKitRoom(roomName));
    }

    @GetMapping("/livekit/rooms/{roomName}/participants")
    @Operation(summary = "LiveKit room 참여자 목록 조회", description = "지정한 LiveKit room에 현재 참여 중인 사용자 목록을 조회합니다.")
    public ApiResponse<AdminResponse.LiveKitParticipantListDTO> listLiveKitParticipants(
            @PathVariable String roomName
    ) {
        return ApiResponse.onSuccess(adminMeetingRoomService.listLiveKitParticipants(roomName));
    }

    @DeleteMapping("/meeting-rooms/{meetingId}/participants/{memberId}")
    @Operation(summary = "관리자용 참여자 강제 제거", description = "지정한 미팅룸에서 특정 참여자를 LiveKit room과 웹소켓 방에서 제거합니다.")
    public ApiResponse<AdminResponse.KickParticipantResponseDTO> removeParticipant(
            @PathVariable Long meetingId,
            @PathVariable Long memberId
    ) {
        return ApiResponse.onSuccess(adminMeetingRoomService.removeParticipant(meetingId, memberId));
    }

    @GetMapping("/meeting-rooms/{meetingId}/websocket-sessions")
    @Operation(summary = "회의 웹소켓 세션 정보 조회", description = "지정한 meetingId에 연결된 웹소켓 세션 정보를 조회합니다.")
    public ApiResponse<AdminResponse.WebSocketSessionListDTO> listWebSocketSessions(
            @PathVariable Long meetingId
    ) {
        return ApiResponse.onSuccess(adminMeetingRoomService.listWebSocketSessions(meetingId));
    }
}
