package com.whylog.server.domain.meeting.service;

import com.whylog.server.global.external.s3.S3Client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingAudioDurationService {

    private static final String FFPROBE_COMMAND = "/opt/homebrew/bin/ffprobe";

    private final S3Client s3Client;

    public Integer resolveAudioDurationSeconds(String audioKey) {
        if (audioKey == null || audioKey.isBlank()) {
            return null;
        }

        String presignedUrl = s3Client.getPresignedFileUrl(audioKey, java.time.Duration.ofMinutes(5));
        if (presignedUrl == null || presignedUrl.isBlank()) {
            return null;
        }

        return probeDurationSeconds(presignedUrl);
    }

    private Integer probeDurationSeconds(String audioUrl) {
        Process process;
        try {
            process = new ProcessBuilder(
                    FFPROBE_COMMAND,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    audioUrl
            ).redirectErrorStream(true).start();
        } catch (IOException e) {
            log.warn("ffprobe 실행 실패: {}", e.getMessage());
            return null;
        }

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.readLine();
        } catch (IOException e) {
            log.warn("ffprobe 출력 읽기 실패: {}", e.getMessage());
            process.destroyForcibly();
            return null;
        }

        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                log.warn("ffprobe 시간 초과");
                process.destroyForcibly();
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("ffprobe 대기 중 인터럽트 발생");
            process.destroyForcibly();
            return null;
        }

        if (process.exitValue() != 0 || output == null || output.isBlank()) {
            return null;
        }

        try {
            double seconds = Double.parseDouble(output.trim());
            if (seconds <= 0) {
                return null;
            }
            return (int) Math.round(seconds);
        } catch (NumberFormatException e) {
            log.warn("오디오 길이 파싱 실패: {}", e.getMessage());
            return null;
        }
    }
}
