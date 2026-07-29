package com.whylog.server.domain.meeting.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class MeetingAudioFileService {

    public static final String AUDIO_FILE_SUFFIX = "-audio";
    public static final String RECORDING_PREFIX = "recordings/meeting-";
    public static final String MP4_EXTENSION = ".mp4";
    public static final String OGG_EXTENSION = ".ogg";

    public String buildRecordingKey(Long meetingId) {
        return RECORDING_PREFIX + meetingId + AUDIO_FILE_SUFFIX + MP4_EXTENSION;
    }

    public String extractFileName(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            return null;
        }

        int lastSlashIndex = audioKey.lastIndexOf('/');
        return lastSlashIndex >= 0 ? audioKey.substring(lastSlashIndex + 1) : audioKey;
    }

    public String alternateKey(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            return null;
        }

        String lowerCase = audioKey.toLowerCase(Locale.ROOT);
        if (lowerCase.endsWith(MP4_EXTENSION)) {
            return audioKey.substring(0, audioKey.length() - MP4_EXTENSION.length()) + OGG_EXTENSION;
        }
        if (lowerCase.endsWith(OGG_EXTENSION)) {
            return audioKey.substring(0, audioKey.length() - OGG_EXTENSION.length()) + MP4_EXTENSION;
        }
        return audioKey;
    }

    public String resolveResponseContentType(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            return null;
        }

        String lowerCase = audioKey.toLowerCase(Locale.ROOT);
        if (lowerCase.endsWith(OGG_EXTENSION)) {
            return "audio/ogg";
        }
        if (lowerCase.endsWith(MP4_EXTENSION)) {
            return "audio/mp4";
        }
        return null;
    }
}
