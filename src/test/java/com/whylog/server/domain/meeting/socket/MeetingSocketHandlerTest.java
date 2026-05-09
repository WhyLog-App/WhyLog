package com.whylog.server.domain.meeting.socket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import com.whylog.server.domain.meeting.socket.message.MeetingMessageType;
import com.whylog.server.domain.meeting.socket.message.MeetingSocketMessage;
import com.whylog.server.domain.meeting.socket.repository.MeetingLiveMessageRepository;
import com.whylog.server.domain.meeting.service.MeetingCommandService;
import com.whylog.server.global.util.json.JsonConverter;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;

@ExtendWith(MockitoExtension.class)
class MeetingSocketHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MeetingSocketRoomService meetingSocketRoomService;

    @Mock
    private MeetingCommandService meetingCommandService;

    private MeetingLiveMessageRepository meetingLiveMessageRepository;
    private MeetingSocketHandler meetingSocketHandler;

    @BeforeEach
    void setUp() {
        meetingLiveMessageRepository = spy(new MeetingLiveMessageRepository());
        meetingSocketHandler = new MeetingSocketHandler(
                meetingSocketRoomService,
                meetingCommandService,
                meetingLiveMessageRepository
        );
    }

    @Test
    void audioTextMessageIsStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession session = mockSession();
        MeetingSocketMessage message = new MeetingSocketMessage(
                MeetingMessageType.AUDIO_TEXT,
                null,
                "안녕하세요",
                null
        );

        meetingSocketHandler.handleTextMessage(session, new TextMessage(JsonConverter.toJson(message)));

        verify(meetingLiveMessageRepository).append(eq(1L), any(LiveMessageEntry.class));
        verify(meetingSocketRoomService).broadcastText(eq(1L), any(String.class));
    }

    @Test
    void chatAndSpeechAreNotStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession chatSession = mockSession();
        org.springframework.web.socket.WebSocketSession speechSession = mockSession();

        meetingSocketHandler.handleTextMessage(
                chatSession,
                new TextMessage(JsonConverter.toJson(new MeetingSocketMessage(MeetingMessageType.CHAT, null, "chat", null)))
        );
        meetingSocketHandler.handleTextMessage(
                speechSession,
                new TextMessage(JsonConverter.toJson(new MeetingSocketMessage(MeetingMessageType.SPEECH, null, "speech", null)))
        );

        verify(meetingLiveMessageRepository, never()).append(any(), any());
    }

    @Test
    void interimAudioTextIsNotStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession session = mockSession();
        MeetingSocketMessage message = new MeetingSocketMessage(
                MeetingMessageType.AUDIO_TEXT,
                null,
                "감",
                objectMapper.valueToTree(Map.of("is_final", false))
        );

        meetingSocketHandler.handleTextMessage(session, new TextMessage(JsonConverter.toJson(message)));

        verify(meetingLiveMessageRepository, never()).append(any(), any());
    }

    @Test
    void finalAudioTextIsStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession session = mockSession();
        MeetingSocketMessage message = new MeetingSocketMessage(
                MeetingMessageType.AUDIO_TEXT,
                null,
                "감사합니다",
                objectMapper.valueToTree(Map.of("is_final", true))
        );

        meetingSocketHandler.handleTextMessage(session, new TextMessage(JsonConverter.toJson(message)));

        verify(meetingLiveMessageRepository).append(eq(1L), any(LiveMessageEntry.class));
    }

    @Test
    void audioTextWithoutIsFinalFieldIsStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession session = mockSession();
        MeetingSocketMessage message = new MeetingSocketMessage(
                MeetingMessageType.AUDIO_TEXT,
                null,
                "감사합니다",
                objectMapper.valueToTree(Map.of("confidence", 0.9))
        );

        meetingSocketHandler.handleTextMessage(session, new TextMessage(JsonConverter.toJson(message)));

        verify(meetingLiveMessageRepository).append(eq(1L), any(LiveMessageEntry.class));
    }

    @Test
    void audioTextWithNullPayloadIsStoredInMemory() throws Exception {
        org.springframework.web.socket.WebSocketSession session = mockSession();
        MeetingSocketMessage message = new MeetingSocketMessage(
                MeetingMessageType.AUDIO_TEXT,
                null,
                "감사합니다",
                null
        );

        meetingSocketHandler.handleTextMessage(session, new TextMessage(JsonConverter.toJson(message)));

        verify(meetingLiveMessageRepository).append(eq(1L), any(LiveMessageEntry.class));
    }

    private org.springframework.web.socket.WebSocketSession mockSession() {
        org.springframework.web.socket.WebSocketSession session = org.mockito.Mockito.mock(org.springframework.web.socket.WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getAttributes()).thenReturn(Map.of(
                MeetingSocketAuthInterceptor.MEETING_ID_ATTRIBUTE, 1L,
                MeetingSocketAuthInterceptor.MEMBER_ID_ATTRIBUTE, 10L,
                MeetingSocketAuthInterceptor.MEMBER_NAME_ATTRIBUTE, "홍길동"
        ));
        return session;
    }
}
