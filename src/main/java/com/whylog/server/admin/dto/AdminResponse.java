package com.whylog.server.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class AdminResponse {

    @Schema(description = "LiveKit 열린 room 목록 응답")
    public record LiveKitRoomListDTO(
            List<LiveKitRoomDTO> rooms
    ) {
    }

    @Schema(description = "LiveKit room 정보")
    public record LiveKitRoomDTO(
            String sid,
            String name,
            Integer numParticipants,
            Boolean activeRecording,
            String creationTime,
            String metadata
    ) {
    }

    @Schema(description = "LiveKit room 삭제 응답")
    public record LiveKitRoomDeleteDTO(
            String roomName,
            Boolean deleted
    ) {
    }

    @Schema(description = "LiveKit room 참여자 목록 응답")
    public record LiveKitParticipantListDTO(
            String roomName,
            List<LiveKitParticipantDTO> participants
    ) {
    }

    @Schema(description = "LiveKit room 참여자 정보")
    public record LiveKitParticipantDTO(
            String sid,
            String identity,
            String name,
            String state,
            String joinedAt,
            Boolean isPublisher,
            String metadata
    ) {
    }

    @Schema(description = "관리자용 미팅 참여자 정보")
    public record ParticipantDTO(
            Long memberId,
            String name,
            String sessionId
    ) {
    }

    @Schema(description = "회의 웹소켓 세션 목록 응답")
    public record WebSocketSessionListDTO(
            Long meetingId,
            List<WebSocketSessionDTO> sessions
    ) {
    }

    @Schema(description = "회의 웹소켓 세션 정보")
    public record WebSocketSessionDTO(
            String sessionId,
            Long memberId,
            String name,
            Boolean open
    ) {
    }

    @Schema(description = "관리자용 참여자 강제 제거 응답")
    public record KickParticipantResponseDTO(
            Long meetingId,
            Long memberId,
            Boolean removed,
            Integer removedSessionCount,
            Boolean liveKitRemoved
    ) {
    }
}
