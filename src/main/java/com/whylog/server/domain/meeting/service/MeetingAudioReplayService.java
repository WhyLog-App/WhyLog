package com.whylog.server.domain.meeting.service;

import com.whylog.server.domain.meeting.dto.MeetingResponse;
import com.whylog.server.domain.meeting.entity.Meeting;
import com.whylog.server.domain.meeting.exception.MeetingAudioNotReadyException;
import com.whylog.server.global.external.s3.S3Client;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MeetingAudioReplayService {

    private static final Duration AUDIO_URL_EXPIRE_DURATION = Duration.ofMinutes(10);

    private final MeetingAudioKeyResolver meetingAudioKeyResolver;
    private final MeetingAudioDurationService meetingAudioDurationService;
    private final MeetingAudioFileService meetingAudioFileService;
    private final S3Client s3Client;

    public MeetingResponse.AudioDTO buildAudioResponse(Meeting meeting) {
        String audioKey = meetingAudioKeyResolver.resolvePlayableAudioKey(meeting)
                .orElseThrow(MeetingAudioNotReadyException::new);
        String audioUrl = buildPlayableAudioUrl(audioKey);

        return MeetingResponse.AudioDTO.builder()
                .meetingId(meeting.getId())
                .audioKey(audioKey)
                .audioUrl(audioUrl)
                .audioDuration(meetingAudioDurationService.resolveAudioDurationSeconds(audioUrl))
                .build();
    }

    public String resolvePlayableAudioUrl(Meeting meeting) {
        String audioKey = meetingAudioKeyResolver.resolvePlayableAudioKey(meeting)
                .orElseThrow(MeetingAudioNotReadyException::new);
        return buildPlayableAudioUrl(audioKey);
    }

    public Integer resolveAudioDurationIfAvailable(Meeting meeting) {
        return meetingAudioKeyResolver.resolvePlayableAudioKey(meeting)
                .map(this::buildPlayableAudioUrl)
                .map(meetingAudioDurationService::resolveAudioDurationSeconds)
                .orElse(null);
    }

    private String buildPlayableAudioUrl(String audioKey) {
        return s3Client.getPresignedFileUrl(
                audioKey,
                AUDIO_URL_EXPIRE_DURATION,
                meetingAudioFileService.resolveResponseContentType(audioKey)
        );
    }
}
