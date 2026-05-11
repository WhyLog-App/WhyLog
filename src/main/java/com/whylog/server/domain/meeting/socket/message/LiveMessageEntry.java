package com.whylog.server.domain.meeting.socket.message;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

// 실시간 회의 발화 한 건을 메모리에 임시 저장하는 단위입니다.
public record LiveMessageEntry(
        Long meetingId,
        Long fromMemberId,
        String fromName,
        Long targetMemberId,
        String text,
        JsonNode payload,
        LocalDateTime receivedAt
) {
}
