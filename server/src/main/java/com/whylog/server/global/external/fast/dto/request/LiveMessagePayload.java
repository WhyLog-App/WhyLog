package com.whylog.server.global.external.fast.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import java.time.Duration;
import java.time.LocalDateTime;

// FastAPI로 전달할 실시간 발화 항목입니다.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LiveMessagePayload(
        Long meetingId,
        Long fromMemberId,
        String fromName,
        String timestamp,
        Long targetMemberId,
        String text,
        JsonNode payload
) {

    @JsonProperty("type")
    public String type() {
        return "TEXT";
    }

    public static LiveMessagePayload from(LiveMessageEntry entry, LocalDateTime meetingStartDateTime) {
        return new LiveMessagePayload(
                entry.meetingId(),
                entry.fromMemberId(),
                entry.fromName(),
                formatElapsed(meetingStartDateTime, entry.receivedAt()),
                entry.targetMemberId(),
                entry.text(),
                entry.payload()
        );
    }

    private static String formatElapsed(LocalDateTime startDateTime, LocalDateTime receivedAt) {
        try {
            if (startDateTime == null || receivedAt == null) {
                return "00:00:00";
            }

            long elapsedSeconds = Duration.between(startDateTime, receivedAt).getSeconds();
            if (elapsedSeconds < 0) {
                return "00:00:00";
            }

            long hours = elapsedSeconds / 3600;
            long minutes = (elapsedSeconds % 3600) / 60;
            long seconds = elapsedSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (RuntimeException exception) {
            return "00:00:00";
        }
    }
}
