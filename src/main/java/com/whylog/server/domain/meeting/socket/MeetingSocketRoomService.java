package com.whylog.server.domain.meeting.socket;

import com.whylog.server.domain.meeting.repository.MeetingRepository;
import com.whylog.server.domain.meeting.socket.message.MeetingEndedMessage;
import com.whylog.server.domain.meeting.socket.message.MeetingMessageType;
import com.whylog.server.domain.meeting.socket.message.ParticipantSummary;
import com.whylog.server.domain.meeting.socket.repository.MeetingRoomRepository;
import com.whylog.server.domain.meeting.socket.repository.MeetingSocketRoomRepository;
import com.whylog.server.global.util.json.JsonConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

// 회의별 참가자 세션 저장소 역할을 하며 텍스트/오디오 메시지 전달을 담당합니다.
@Service
public class MeetingSocketRoomService {

    private final MeetingRepository meetingRepository;
    private final MeetingSocketRoomRepository meetingSocketRoomRepository;
    private final Executor meetingSocketDispatchExecutor;

    public MeetingSocketRoomService(
            MeetingRepository meetingRepository,
            MeetingSocketRoomRepository meetingSocketRoomRepository,
            @Qualifier("meetingSocketDispatchExecutor") Executor meetingSocketDispatchExecutor
    ) {
        this.meetingRepository = meetingRepository;
        this.meetingSocketRoomRepository = meetingSocketRoomRepository;
        this.meetingSocketDispatchExecutor = meetingSocketDispatchExecutor;
    }

    // 웹소켓으로 연결하려는 회의가 DB에 실제로 존재하는지 확인합니다.
    @Transactional(readOnly = true)
    public boolean existsMeeting(Long meetingId) {
        return meetingRepository.existsById(meetingId);
    }

    // 메모리 상에 회의방 엔트리가 없으면 새로 생성합니다.
    public void createRoomIfAbsent(Long meetingId) {
        meetingSocketRoomRepository.getOrCreate(meetingId);
    }

    // 회의 종료 등으로 더 이상 사용하지 않는 회의방 엔트리를 제거합니다.
    public void closeRoom(Long meetingId) {
        meetingSocketRoomRepository.delete(meetingId);
    }

    // 참가자 세션을 해당 회의방에 입장시킵니다.
    public void join(MeetingParticipant participant) {
        MeetingRoomRepository room = getOrCreateRoom(participant.meetingId());
        room.addParticipant(participant);
    }

    // 참가자 세션을 회의방에서 제거하고, 방이 비면 엔트리도 함께 정리합니다.
    public MeetingParticipant leave(Long meetingId, String sessionId) {
        MeetingRoomRepository room = getRoom(meetingId);
        if (room == null) {
            return null;
        }

        MeetingParticipant removed = room.removeParticipant(sessionId);
        if (room.isEmpty()) {
            closeRoom(meetingId);
        }
        return removed;
    }

    // 특정 멤버가 회의방에 이미 연결되어 있는지 확인합니다.
    public boolean existsParticipant(Long meetingId, Long memberId) {
        MeetingRoomRepository room = getRoom(meetingId);
        if (room == null) {
            return false;
        }

        return !room.participantsByMemberId(memberId).isEmpty();
    }

    // 클라이언트에 보여 줄 현재 참가자 목록을 이름순으로 반환합니다.
    public List<ParticipantSummary> listParticipants(Long meetingId) {
        MeetingRoomRepository room = getRoom(meetingId);
        if (room == null) {
            return List.of();
        }

        return room.participants().stream()
                .map(ParticipantSummary::create)
                .sorted(Comparator.comparing(ParticipantSummary::name))
                .toList();
    }

    // 텍스트 메시지를 회의방의 모든 참가자에게 브로드캐스트합니다.
    public void broadcastText(Long meetingId, String payload) {
        dispatch(() -> broadcast(
                meetingId,
                participant -> new TextMessage(payload),
                participant -> false));
    }

    // 채팅 메시지를 별도 경로로 브로드캐스트합니다.
    public void broadcastChatText(Long meetingId, String payload) {
        dispatch(() -> broadcast(
                meetingId,
                participant -> new TextMessage(payload),
                participant -> false));
    }

    // 실시간 자막/STT 메시지를 채팅과 분리해 브로드캐스트합니다.
    public void broadcastSpeechText(Long meetingId, String payload) {
        dispatch(() -> broadcast(
                meetingId,
                participant -> new TextMessage(payload),
                participant -> false));
    }

    // 오디오 텍스트 변환 결과를 별도 경로로 브로드캐스트합니다.
    public void broadcastAudioText(Long meetingId, String payload) {
        dispatch(() -> broadcast(
                meetingId,
                participant -> new TextMessage(payload),
                participant -> false));
    }

    // 특정 대상 참가자 한 명에게만 시그널링 메시지를 전달합니다.
    public void sendToMember(Long meetingId, Long targetMemberId, String payload) {
        dispatch(() -> sendToMemberInternal(meetingId, targetMemberId, payload));
    }

    // 회의 종료 메시지를 현재 회의방 참가자 전체에게 전송합니다.
    public void broadcastMeetingEnded(Long meetingId, LocalDateTime endedAt) {
        broadcastText(
                meetingId,
                JsonConverter.toJson(new MeetingEndedMessage(
                        MeetingMessageType.MEETING_ENDED,
                        meetingId,
                        endedAt
                ))
        );
    }

    // 회의방이 이미 있으면 반환하고, 없으면 새로 생성해서 반환합니다.
    private MeetingRoomRepository getOrCreateRoom(Long meetingId) {
        return meetingSocketRoomRepository.getOrCreate(meetingId);
    }

    // 메모리에 올라와 있는 회의방 저장소를 조회합니다.
    private MeetingRoomRepository getRoom(Long meetingId) {
        return meetingSocketRoomRepository.findByMeetingId(meetingId);
    }

    private void dispatch(Runnable task) {
        try {
            meetingSocketDispatchExecutor.execute(task);
        } catch (RuntimeException exception) {
            task.run();
        }
    }

    private void sendToMemberInternal(Long meetingId, Long targetMemberId, String payload) {
        MeetingRoomRepository room = getRoom(meetingId);
        if (room == null) {
            return;
        }

        room.participantsByMemberId(targetMemberId).forEach(participant -> {
            if (!sendMessage(participant, new TextMessage(payload))) {
                leave(meetingId, participant.sessionId());
            }
        });
    }

    // 회의방 참가자 전체를 순회하면서 메시지를 보내고 끊어진 세션은 정리합니다.
    private void broadcast(
            Long meetingId,
            Function<MeetingParticipant, WebSocketMessage<?>> messageFactory,
            Function<MeetingParticipant, Boolean> skipCondition
    ) {

        MeetingRoomRepository room = getRoom(meetingId);
        if (room == null) {
            return;
        }

        List<MeetingParticipant> disconnectedParticipants = new ArrayList<>();
        for (MeetingParticipant participant : new ArrayList<>(room.participants())) {
            if (skipCondition.apply(participant)) {
                continue;
            }

            if (!sendMessage(participant, messageFactory.apply(participant))) {
                disconnectedParticipants.add(participant);
            }
        }

        cleanupDisconnectedParticipants(meetingId, disconnectedParticipants);
    }

    // 단일 참가자에게 메시지를 전송하고 성공 여부를 반환합니다.
    private boolean sendMessage(MeetingParticipant participant, WebSocketMessage<?> message) {
        if (!participant.socketSession().isOpen()) {
            return false;
        }

        try {
            participant.socketSession().sendMessage(message);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    // 전송 중 끊어진 세션들을 회의방에서 제거합니다.
    private void cleanupDisconnectedParticipants(Long meetingId, List<MeetingParticipant> disconnectedParticipants) {
        disconnectedParticipants.forEach(participant -> leave(meetingId, participant.sessionId()));
    }
}
