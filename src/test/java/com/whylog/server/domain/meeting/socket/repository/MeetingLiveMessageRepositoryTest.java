package com.whylog.server.domain.meeting.socket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeetingLiveMessageRepositoryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MeetingLiveMessageRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MeetingLiveMessageRepository();
    }

    @Test
    void appendingSameSpeakerAndSameTextConsecutivelyKeepsOneEntry() {
        LiveMessageEntry first = entry(1L, 10L, "감사합니다");
        LiveMessageEntry duplicate = entry(1L, 10L, "감사합니다");

        repository.append(1L, first);
        repository.append(1L, duplicate);

        assertThat(repository.drain(1L)).hasSize(1);
    }

    @Test
    void sameTextFromDifferentSpeakerIsStoredSeparately() {
        repository.append(1L, entry(1L, 10L, "감사합니다"));
        repository.append(1L, entry(1L, 11L, "감사합니다"));

        assertThat(repository.drain(1L)).hasSize(2);
    }

    @Test
    void sameSpeakerWithDifferentTextIsStoredSeparately() {
        repository.append(1L, entry(1L, 10L, "감사합니다"));
        repository.append(1L, entry(1L, 10L, "네"));

        assertThat(repository.drain(1L)).hasSize(2);
    }

    @Test
    void dedupOnlyAppliesToImmediatelyPreviousEntry() {
        repository.append(1L, entry(1L, 10L, "감사합니다"));
        repository.append(1L, entry(1L, 11L, "네"));
        repository.append(1L, entry(1L, 10L, "감사합니다"));

        assertThat(repository.drain(1L)).hasSize(3);
    }

    @Test
    void drainResetsDedupState() {
        repository.append(1L, entry(1L, 10L, "감사합니다"));
        assertThat(repository.drain(1L)).hasSize(1);

        repository.append(1L, entry(1L, 10L, "감사합니다"));
        assertThat(repository.drain(1L)).hasSize(1);
    }

    private LiveMessageEntry entry(Long meetingId, Long fromMemberId, String text) {
        return new LiveMessageEntry(
                meetingId,
                fromMemberId,
                "홍길동",
                null,
                text,
                objectMapper.valueToTree(Map.of("is_final", true)),
                LocalDateTime.of(2026, 1, 1, 10, 0, 0)
        );
    }
}
