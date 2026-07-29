package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.global.external.s3.S3Client;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingAudioKeyResolver {

    private final MeetingAudioFileService meetingAudioFileService;
    private final S3Client s3Client;

    public Optional<String> resolvePlayableAudioKey(Meeting meeting) {
        String audioKey = meeting.getAudioKey();
        if (isPlayable(audioKey)) {
            return Optional.of(audioKey);
        }

        String alternateAudioKey = meetingAudioFileService.alternateKey(audioKey);
        if (isPlayable(alternateAudioKey)) {
            return Optional.of(alternateAudioKey);
        }

        return Optional.empty();
    }

    private boolean isPlayable(String audioKey) {
        return audioKey != null && !audioKey.isBlank() && s3Client.exists(audioKey);
    }
}
