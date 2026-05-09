package com.whylog.server.domain.meeting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import com.whylog.server.domain.meeting.socket.repository.MeetingLiveMessageRepository;
import com.whylog.server.global.external.fast.dto.request.LiveMessagePayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 회의 종료 시 실시간 발화를 drain해서 FastAPI 전송용 JSON으로 묶습니다.
@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingLiveMessageBundleService {

    private final MeetingLiveMessageRepository meetingLiveMessageRepository;
    private final ObjectMapper objectMapper;

    public String buildLiveMessagesJson(Meeting meeting) {
        List<LiveMessageEntry> liveMessageEntries = meetingLiveMessageRepository.drain(meeting.getId());
        if (liveMessageEntries.isEmpty()) {
            return null;
        }

        List<LiveMessagePayload> payloads = liveMessageEntries.stream()
                .map(entry -> LiveMessagePayload.from(entry, meeting.getStartDateTime()))
                .toList();

        try {
            return objectMapper.writeValueAsString(payloads);
        } catch (JsonProcessingException exception) {
            log.warn("실시간 발화 직렬화 실패: meetingId={}", meeting.getId(), exception);
            return null;
        }
    }
}
