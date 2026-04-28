package com.whylog.server.domain.meeting.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingAudioDurationService {

    @Value("${audio.ffprobe-command}")
    private String ffprobeCommand;

    public Integer resolveAudioDurationSeconds(String audioUrl) {
        if (audioUrl == null || audioUrl.isBlank()) {
            return null;
        }

        return probeDurationSeconds(audioUrl);
    }

    private Integer probeDurationSeconds(String audioUrl) {
        Process process;
        try {
            process = new ProcessBuilder(
                    ffprobeCommand,
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
