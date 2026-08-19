package com.whylog.server.admin.service;

import com.whylog.server.admin.dto.AdminResponse;
import com.whylog.server.domain.meeting.service.MeetingCommandService;
import com.whylog.server.domain.meeting.socket.MeetingParticipant;
import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.meeting.socket.message.MeetingMessageType;
import com.whylog.server.domain.meeting.socket.message.ParticipantLeftMessage;
import com.whylog.server.domain.meeting.socket.message.ParticipantSummary;
import com.whylog.server.domain.meeting.socket.message.RosterMessage;
import com.whylog.server.domain.meeting.socket.repository.MeetingRoomRepository;
import com.whylog.server.domain.meeting.socket.repository.MeetingSocketRoomRepository;
import com.whylog.server.global.util.json.JsonConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.CloseStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMeetingRoomService {

    private final MeetingSocketRoomRepository meetingSocketRoomRepository;
    private final MeetingSocketRoomService meetingSocketRoomService;
    private final MeetingCommandService meetingCommandService;

    public AdminResponse.WebSocketSessionListDTO listWebSocketSessions(Long meetingId) {
        MeetingRoomRepository room = meetingSocketRoomRepository.findByMeetingId(meetingId);
        List<AdminResponse.WebSocketSessionDTO> sessions = new ArrayList<>();
        if (room != null) {
            sessions =
                    room.participants().stream()
                            .map(this::toWebSocketSessionDTO)
                            .sorted(
                                    Comparator.comparing(
                                            AdminResponse.WebSocketSessionDTO::sessionId))
                            .toList();
        }

        return new AdminResponse.WebSocketSessionListDTO(meetingId, sessions);
    }

    @Transactional
    public AdminResponse.KickParticipantResponseDTO removeParticipant(
            Long meetingId, Long memberId) {
        MeetingRoomRepository room = meetingSocketRoomRepository.findByMeetingId(meetingId);
        if (room == null) {
            return new AdminResponse.KickParticipantResponseDTO(meetingId, memberId, false, 0);
        }

        List<MeetingParticipant> removedParticipants = room.removeParticipantsByMemberId(memberId);
        if (removedParticipants.isEmpty()) {
            return new AdminResponse.KickParticipantResponseDTO(meetingId, memberId, false, 0);
        }

        broadcastParticipantRemoval(meetingId, removedParticipants);
        closeRemovedSessions(removedParticipants);

        if (room.isEmpty()) {
            meetingSocketRoomRepository.delete(meetingId);
            meetingCommandService.autoEndMeetingIfEmpty(meetingId);
        }

        return new AdminResponse.KickParticipantResponseDTO(
                meetingId, memberId, true, removedParticipants.size());
    }

    private AdminResponse.WebSocketSessionDTO toWebSocketSessionDTO(
            MeetingParticipant participant) {
        return new AdminResponse.WebSocketSessionDTO(
                participant.sessionId(),
                participant.memberId(),
                participant.name(),
                participant.socketSession() != null && participant.socketSession().isOpen());
    }

    private void broadcastParticipantRemoval(
            Long meetingId, List<MeetingParticipant> removedParticipants) {
        if (removedParticipants.isEmpty()) {
            return;
        }

        MeetingParticipant removedParticipant = removedParticipants.get(0);
        meetingSocketRoomService.broadcastText(
                meetingId,
                JsonConverter.toJson(
                        new ParticipantLeftMessage(
                                MeetingMessageType.PARTICIPANT_LEFT,
                                meetingId,
                                removedParticipant.memberId(),
                                removedParticipant.name(),
                                java.time.Instant.now().toString())));

        List<ParticipantSummary> participants =
                meetingSocketRoomService.listParticipants(meetingId);
        if (!participants.isEmpty()) {
            meetingSocketRoomService.broadcastText(
                    meetingId, JsonConverter.toJson(RosterMessage.create(meetingId, participants)));
        }
    }

    private void closeRemovedSessions(List<MeetingParticipant> removedParticipants) {
        for (MeetingParticipant participant : removedParticipants) {
            try {
                if (participant.socketSession().isOpen()) {
                    participant.socketSession().close(CloseStatus.NORMAL);
                }
            } catch (IOException exception) {
                log.warn(
                        "Failed to close kicked participant session: meetingId={}, memberId={}, sessionId={}",
                        participant.meetingId(),
                        participant.memberId(),
                        participant.sessionId(),
                        exception);
            }
        }
    }
}
