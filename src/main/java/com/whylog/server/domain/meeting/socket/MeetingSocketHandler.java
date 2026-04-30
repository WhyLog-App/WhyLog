package com.whylog.server.domain.meeting.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.whylog.server.domain.meeting.socket.message.*;
import com.whylog.server.domain.meeting.service.MeetingCommandService;
import com.whylog.server.global.util.json.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

// 회의 웹소켓 연결의 입장, 퇴장, 텍스트 메시지, 오디오 바이너리 중계를 처리합니다.
@Component
@Slf4j
@RequiredArgsConstructor
public class MeetingSocketHandler extends BinaryWebSocketHandler {

    private static final int SESSION_SEND_TIME_LIMIT_MS = 10_000;
    private static final int SESSION_BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;

    private final MeetingSocketRoomService meetingSocketRoomService;
    private final MeetingCommandService meetingCommandService;

    // 웹소켓 연결 직후 참가자를 방에 등록하고 현재 참여자 목록과 입장 이벤트를 전파합니다.
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        MeetingParticipant participant = createParticipant(session, decorate(session));
        if (!meetingSocketRoomService.existsMeeting(participant.meetingId())) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        meetingSocketRoomService.join(participant);

        ConnectedMessage connectedMessage = ConnectedMessage.create(
                participant, participantSummaries(participant.meetingId())
        );
        session.sendMessage(new TextMessage(JsonConverter.toJson(connectedMessage)));

        broadcastRoster(participant.meetingId());
        meetingSocketRoomService.broadcastText(
                participant.meetingId(),
                JsonConverter.toJson(ParticipantJoinedMessage.create(participant))
        );
    }

    // 채팅, 자막, 시그널링 같은 텍스트 기반 회의 메시지를 파싱해 브로드캐스트 또는 단건 전달합니다.
    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        MeetingParticipant participant = createParticipant(session);
        MeetingSocketMessage incoming;
        try {
            incoming = JsonConverter.readValue(message, MeetingSocketMessage.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid websocket message payload", exception);
        }

        MeetingMessageType type = incoming.type();
        if (type == null) {
            sendError(session, "Unsupported message type: null");
            return;
        }

        switch (type) {
            case CHAT -> {
                logIncomingText(participant, type, incoming);
                meetingSocketRoomService.broadcastChatText(
                        participant.meetingId(),
                        JsonConverter.toJson(MeetingTextMessage.createTextMessage(
                                participant,
                                type,
                                null,
                                Optional.ofNullable(incoming.text()).orElse(""),
                                incoming.payload()
                        ))
                );
            }
            case SPEECH -> {
                logIncomingText(participant, type, incoming);
                meetingSocketRoomService.broadcastSpeechText(
                        participant.meetingId(),
                        JsonConverter.toJson(MeetingTextMessage.createTextMessage(
                                participant,
                                type,
                                null,
                                Optional.ofNullable(incoming.text()).orElse(""),
                                incoming.payload()
                        ))
                );
            }
            case AUDIO_TEXT -> {
                logIncomingText(participant, type, incoming);
                meetingSocketRoomService.broadcastAudioText(
                        participant.meetingId(),
                        JsonConverter.toJson(MeetingTextMessage.createTextMessage(
                                participant,
                                type,
                                null,
                                Optional.ofNullable(incoming.text()).orElse(""),
                                incoming.payload()
                        ))
                );
            }
            case OFFER, ANSWER, ICE -> {
                if (incoming.targetMemberId() == null) {
                    sendError(session, "targetMemberId is required for " + type.value());
                    return;
                }

                meetingSocketRoomService.sendToMember(
                        participant.meetingId(),
                        incoming.targetMemberId(),
                        JsonConverter.toJson(MeetingTextMessage.createTextMessage(
                                participant,
                                type,
                                incoming.targetMemberId(),
                                null,
                                incoming.payload()
                        ))
                );
            }
            default -> sendError(session, "Unsupported message type: " + type.value());
        }
    }

    // 실시간 오디오는 WebRTC/SFU 경로로 전달하고, 웹소켓 바이너리 프레임은 더 이상 중계하지 않습니다.
    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, BinaryMessage message) throws Exception {
        sendError(session, "Binary audio relay over WebSocket is not supported. Use WebRTC transport for live audio.");
    }

    // 정상 종료된 세션을 회의방에서 제거하고 퇴장 이벤트를 전파합니다.
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        removeParticipant(session);
    }

    // 전송 오류가 발생한 세션을 정리하고 필요하면 서버 에러 상태로 연결을 닫습니다.
    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        removeParticipant(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // 세션을 회의방에서 제거하고 퇴장 이벤트 및 갱신된 roster를 방송합니다.
    private void removeParticipant(WebSocketSession session) {
        Long meetingId = getAttribute(session, MeetingSocketAuthInterceptor.MEETING_ID_ATTRIBUTE, Long.class);
        if (meetingId == null) {
            return;
        }

        MeetingParticipant removed = meetingSocketRoomService.leave(meetingId, session.getId());
        if (removed == null) {
            return;
        }

        meetingSocketRoomService.broadcastText(meetingId, JsonConverter.toJson(new ParticipantLeftMessage(
                MeetingMessageType.PARTICIPANT_LEFT,
                meetingId,
                removed.memberId(),
                removed.name(),
                now()
        )));
        broadcastRoster(meetingId);

        if (meetingSocketRoomService.listParticipants(meetingId).isEmpty()) {
            meetingCommandService.autoEndMeetingIfEmpty(meetingId);
        }
    }

    // 현재 회의 참가자 목록을 모든 클라이언트에 전파합니다.
    private void broadcastRoster(Long meetingId) {
        meetingSocketRoomService.broadcastText(
                meetingId,
                JsonConverter.toJson(RosterMessage.create(meetingId, participantSummaries(meetingId)))
        );
    }

    // 핸드셰이크에서 저장한 속성으로 참가자 정보를 복원합니다.
    private MeetingParticipant createParticipant(WebSocketSession session) {
        return createParticipant(session, session);
    }

    private MeetingParticipant createParticipant(WebSocketSession session, WebSocketSession outboundSession) {
        Long meetingId = getAttribute(session, MeetingSocketAuthInterceptor.MEETING_ID_ATTRIBUTE, Long.class);
        Long memberId = getAttribute(session, MeetingSocketAuthInterceptor.MEMBER_ID_ATTRIBUTE, Long.class);
        String name = getAttribute(session, MeetingSocketAuthInterceptor.MEMBER_NAME_ATTRIBUTE, String.class);

        if (meetingId == null || memberId == null || !StringUtils.hasText(name)) {
            throw new IllegalStateException("WebSocket participant attributes are missing");
        }

        return new MeetingParticipant(session.getId(), memberId, name, meetingId, outboundSession);
    }

    // 잘못된 요청이나 지원하지 않는 타입에 대한 에러 메시지를 클라이언트에 보냅니다.
    private void sendError(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(
                    JsonConverter.toJson(
                            new ErrorMessage(
                                    MeetingMessageType.ERROR, message
                            )
                    )));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to send websocket error message", exception);
        }
    }

    // 웹소켓 세션 속성에서 지정한 타입의 값을 안전하게 꺼냅니다.
    private <T> T getAttribute(WebSocketSession session, String key, Class<T> type) {
        Object value = session.getAttributes().get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    private List<ParticipantSummary> participantSummaries(Long meetingId) {
        return meetingSocketRoomService.listParticipants(meetingId);
    }

    // 웹소켓 메시지에 사용할 현재 시각 문자열을 생성합니다.
        private String now() {
        return Instant.now().toString();
    }

    private String logIncomingText(MeetingParticipant participant, MeetingMessageType type, MeetingSocketMessage incoming) {
        String text = Optional.ofNullable(incoming.text()).orElse("");
        log.info(
                "meeting text received: meetingId={}, memberId={}, name={}, type={}, targetMemberId={}, text={}",
                participant.meetingId(),
                participant.memberId(),
                participant.name(),
                type.value(),
                incoming.targetMemberId(),
                text
        );
        return text;
    }

    private WebSocketSession decorate(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(
                session,
                SESSION_SEND_TIME_LIMIT_MS,
                SESSION_BUFFER_SIZE_LIMIT_BYTES
        );
    }

}
