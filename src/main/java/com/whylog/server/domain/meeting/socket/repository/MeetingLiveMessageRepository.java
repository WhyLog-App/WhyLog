package com.whylog.server.domain.meeting.socket.repository;

import com.whylog.server.domain.meeting.socket.message.LiveMessageEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

// meetingId 기준으로 실시간 발화를 메모리에 임시 저장합니다.
@Repository
public class MeetingLiveMessageRepository {

    private final Map<Long, CopyOnWriteArrayList<LiveMessageEntry>> liveMessagesByMeetingId = new ConcurrentHashMap<>();

    public void append(Long meetingId, LiveMessageEntry entry) {
        liveMessagesByMeetingId.compute(meetingId, (id, list) -> {
            CopyOnWriteArrayList<LiveMessageEntry> target = list != null ? list : new CopyOnWriteArrayList<>();
            if (!target.isEmpty()) {
                LiveMessageEntry last = target.get(target.size() - 1);
                if (isSameUtterance(last, entry)) {
                    return target;
                }
            }

            target.add(entry);
            return target;
        });
    }

    public List<LiveMessageEntry> drain(Long meetingId) {
        CopyOnWriteArrayList<LiveMessageEntry> entries = liveMessagesByMeetingId.remove(meetingId);
        if (entries == null) {
            return List.of();
        }

        return List.copyOf(entries);
    }

    public void clear(Long meetingId) {
        liveMessagesByMeetingId.remove(meetingId);
    }

    private static boolean isSameUtterance(LiveMessageEntry left, LiveMessageEntry right) {
        return Objects.equals(left.fromMemberId(), right.fromMemberId())
                && Objects.equals(left.text(), right.text());
    }
}
