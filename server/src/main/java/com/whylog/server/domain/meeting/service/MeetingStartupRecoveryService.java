package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingStartupRecoveryService implements ApplicationRunner {

    private final MeetingRepository meetingRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LocalDateTime recoveryTime = LocalDateTime.now();
        int updatedCount = meetingRepository.markAllOngoingMeetingsAsEnded(recoveryTime);

        if (updatedCount > 0) {
            log.warn("Recovered ongoing meetings on startup: count={}, endedAt={}", updatedCount, recoveryTime);
        } else {
            log.info("No ongoing meetings needed recovery on startup.");
        }
    }
}
