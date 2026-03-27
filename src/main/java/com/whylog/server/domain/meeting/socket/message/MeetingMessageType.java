package com.whylog.server.domain.meeting.socket.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

// 웹소켓에서 주고받는 메시지 타입 상수입니다.
public enum MeetingMessageType {
    CONNECTED("connected"),
    PARTICIPANT_JOINED("participant_joined"),
    PARTICIPANT_LEFT("participant_left"),
    ROSTER("roster"),
    ERROR("error"),
    CHAT("chat"),
    SPEECH("speech"),
    AUDIO_TEXT("audio_text"),
    OFFER("offer"),
    ANSWER("answer"),
    ICE("ice");

    private final String value;

    MeetingMessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static MeetingMessageType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
