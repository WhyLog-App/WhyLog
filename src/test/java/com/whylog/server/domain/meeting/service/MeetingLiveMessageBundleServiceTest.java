package com.whylog.server.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import com.whylog.server.domain.meeting.socket.repository.MeetingLiveMessageRepository;
import com.whylog.server.domain.team.entity.Team;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MeetingLiveMessageBundleServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MeetingLiveMessageRepository meetingLiveMessageRepository;
    private MeetingLiveMessageBundleService meetingLiveMessageBundleService;

    @BeforeEach
    void setUp() {
        meetingLiveMessageRepository = new MeetingLiveMessageRepository();
        meetingLiveMessageBundleService = new MeetingLiveMessageBundleService(meetingLiveMessageRepository, objectMapper);
    }

    @Test
    void buildLiveMessagesJsonSerializesDrainedEntries() throws Exception {
        Meeting meeting = meeting();
        meetingLiveMessageRepository.append(
                meeting.getId(),
                new LiveMessageEntry(
                        meeting.getId(),
                        2L,
                        "발화자",
                        null,
                        "안녕하세요",
                        objectMapper.valueToTree(Map.of("foo", "bar")),
                        LocalDateTime.of(2026, 1, 1, 10, 2, 3)
                )
        );

        String json = meetingLiveMessageBundleService.buildLiveMessagesJson(meeting);

        assertThat(json).isNotBlank();
        assertThat(objectMapper.readTree(json)).hasSize(1);
        assertThat(objectMapper.readTree(json).get(0).get("type").asText()).isEqualTo("TEXT");
        assertThat(objectMapper.readTree(json).get(0).get("timestamp").asText()).isEqualTo("01:02:03");
        assertThat(meetingLiveMessageRepository.drain(meeting.getId())).isEmpty();
    }

    @Test
    void buildLiveMessagesJsonReturnsNullWhenNoEntriesExist() {
        Meeting meeting = meeting();

        String json = meetingLiveMessageBundleService.buildLiveMessagesJson(meeting);

        assertThat(json).isNull();
    }

    private Meeting meeting() {
        Meeting meeting = Meeting.create(
                com.whylog.server.domain.meeting.dto.MeetingRequest.MeetingCreateDTO.builder().name("회의").build(),
                org.mockito.Mockito.mock(Team.class)
        );
        ReflectionTestUtils.setField(meeting, "id", 123L);
        ReflectionTestUtils.setField(meeting, "startDateTime", LocalDateTime.of(2026, 1, 1, 9, 0, 0));
        return meeting;
    }
}
