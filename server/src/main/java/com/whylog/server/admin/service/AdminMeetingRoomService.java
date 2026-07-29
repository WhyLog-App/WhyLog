package com.whylog.server.admin.service;

import com.whylog.server.admin.dto.AdminResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.meeting.service.MeetingCommandService;
import com.whylog.server.domain.meeting.service.LiveKitTokenService;
import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.meeting.socket.message.ParticipantLeftMessage;
import com.whylog.server.domain.meeting.socket.message.MeetingMessageType;
import com.whylog.server.domain.meeting.socket.message.ParticipantSummary;
import com.whylog.server.domain.meeting.socket.message.RosterMessage;
import com.whylog.server.domain.meeting.socket.repository.MeetingRoomRepository;
import com.whylog.server.domain.meeting.socket.repository.MeetingSocketRoomRepository;
import com.whylog.server.global.external.livekit.LiveKitEgressClient;
import com.whylog.server.global.util.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMeetingRoomService {

    private final MeetingRepository meetingRepository;
    private final MeetingSocketRoomRepository meetingSocketRoomRepository;
    private final MeetingSocketRoomService meetingSocketRoomService;
    private final MeetingCommandService meetingCommandService;
    private final LiveKitTokenService liveKitTokenService;
    private final LiveKitEgressClient liveKitEgressClient;

    @Transactional(readOnly = true)
    public AdminResponse.LiveKitRoomListDTO listLiveKitRooms() {
        String roomListToken = liveKitTokenService.createRoomListToken("room-admin");
        List<AdminResponse.LiveKitRoomDTO> rooms = liveKitEgressClient.listRooms(roomListToken).stream()
                .map(this::toLiveKitRoomDTO)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AdminResponse.LiveKitRoomDTO::name))
                .toList();

        return new AdminResponse.LiveKitRoomListDTO(rooms);
    }

    public AdminResponse.LiveKitRoomDeleteDTO deleteLiveKitRoom(String roomName) {
        String roomCreateToken = liveKitTokenService.createRoomCreateToken("room-admin");
        liveKitEgressClient.deleteRoom(roomCreateToken, roomName);
        return new AdminResponse.LiveKitRoomDeleteDTO(roomName, true);
    }

    public AdminResponse.LiveKitParticipantListDTO listLiveKitParticipants(String roomName) {
        String roomAdminToken = liveKitTokenService.createRoomAdminToken("room-admin", roomName);
        List<AdminResponse.LiveKitParticipantDTO> participants = liveKitEgressClient.listParticipants(roomAdminToken, roomName).stream()
                .map(this::toLiveKitParticipantDTO)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(AdminResponse.LiveKitParticipantDTO::identity, Comparator.nullsLast(String::compareTo)))
                .toList();

        return new AdminResponse.LiveKitParticipantListDTO(roomName, participants);
    }

    public AdminResponse.WebSocketSessionListDTO listWebSocketSessions(Long meetingId) {
        MeetingRoomRepository room = meetingSocketRoomRepository.findByMeetingId(meetingId);
        List<AdminResponse.WebSocketSessionDTO> sessions = new ArrayList<>();
        if (room != null) {
            sessions = room.participants().stream()
                    .map(this::toWebSocketSessionDTO)
                    .sorted(Comparator.comparing(AdminResponse.WebSocketSessionDTO::sessionId))
                    .toList();
        }

        return new AdminResponse.WebSocketSessionListDTO(meetingId, sessions);
    }

    @Transactional
    public AdminResponse.KickParticipantResponseDTO removeParticipant(Long meetingId, Long memberId) {
        MeetingRoomRepository room = meetingSocketRoomRepository.findByMeetingId(meetingId);
        if (room == null) {
            return new AdminResponse.KickParticipantResponseDTO(meetingId, memberId, false, 0, false);
        }

        List<MeetingParticipant> removedParticipants = room.removeParticipantsByMemberId(memberId);
        if (removedParticipants.isEmpty()) {
            return new AdminResponse.KickParticipantResponseDTO(meetingId, memberId, false, 0, false);
        }

        boolean liveKitRemoved = false;
        try {
            String roomName = buildRoomName(meetingId);
            String roomAdminToken = liveKitTokenService.createRoomAdminToken("room-admin", roomName);
            liveKitEgressClient.removeParticipant(roomAdminToken, roomName, String.valueOf(memberId));
            liveKitRemoved = true;
        } catch (RuntimeException exception) {
            log.warn("LiveKit participant removal failed: meetingId={}, memberId={}", meetingId, memberId, exception);
        }

        broadcastParticipantRemoval(meetingId, removedParticipants);
        closeRemovedSessions(removedParticipants);

        if (room.isEmpty()) {
            meetingSocketRoomRepository.delete(meetingId);
            meetingCommandService.autoEndMeetingIfEmpty(meetingId);
        }

        return new AdminResponse.KickParticipantResponseDTO(
                meetingId,
                memberId,
                true,
                removedParticipants.size(),
                liveKitRemoved
        );
    }

    private AdminResponse.LiveKitRoomDTO toLiveKitRoomDTO(Map<String, Object> room) {
        if (room == null) {
            return null;
        }

        return new AdminResponse.LiveKitRoomDTO(
                stringValue(room.get("sid")),
                stringValue(room.get("name")),
                intValue(room.get("num_participants")),
                booleanValue(room.get("active_recording")),
                stringValue(room.get("creation_time")),
                stringValue(room.get("metadata"))
        );
    }

    private AdminResponse.LiveKitParticipantDTO toLiveKitParticipantDTO(Map<String, Object> participant) {
        if (participant == null) {
            return null;
        }

        return new AdminResponse.LiveKitParticipantDTO(
                stringValue(participant.get("sid")),
                stringValue(participant.get("identity")),
                stringValue(participant.get("name")),
                stringValue(participant.get("state")),
                stringValue(participant.get("joined_at")),
                booleanValue(participant.get("is_publisher")),
                stringValue(participant.get("metadata"))
        );
    }

    private AdminResponse.WebSocketSessionDTO toWebSocketSessionDTO(MeetingParticipant participant) {
        return new AdminResponse.WebSocketSessionDTO(
                participant.sessionId(),
                participant.memberId(),
                participant.name(),
                participant.socketSession() != null && participant.socketSession().isOpen()
        );
    }

    private void broadcastParticipantRemoval(Long meetingId, List<MeetingParticipant> removedParticipants) {
        if (removedParticipants.isEmpty()) {
            return;
        }

        MeetingParticipant removedParticipant = removedParticipants.get(0);
        meetingSocketRoomService.broadcastText(
                meetingId,
                JsonConverter.toJson(new ParticipantLeftMessage(
                        MeetingMessageType.PARTICIPANT_LEFT,
                        meetingId,
                        removedParticipant.memberId(),
                        removedParticipant.name(),
                        java.time.Instant.now().toString()
                ))
        );

        List<ParticipantSummary> participants = meetingSocketRoomService.listParticipants(meetingId);
        if (!participants.isEmpty()) {
            meetingSocketRoomService.broadcastText(
                    meetingId,
                    JsonConverter.toJson(RosterMessage.create(meetingId, participants))
            );
        }
    }

    private void closeRemovedSessions(List<MeetingParticipant> removedParticipants) {
        for (MeetingParticipant participant : removedParticipants) {
            try {
                if (participant.socketSession().isOpen()) {
                    participant.socketSession().close(CloseStatus.NORMAL);
                }
            } catch (IOException exception) {
                log.warn("Failed to close kicked participant session: meetingId={}, memberId={}, sessionId={}",
                        participant.meetingId(), participant.memberId(), participant.sessionId(), exception);
            }
        }
    }

    private String buildRoomName(Long meetingId) {
        return "meeting-" + meetingId;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return null;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
