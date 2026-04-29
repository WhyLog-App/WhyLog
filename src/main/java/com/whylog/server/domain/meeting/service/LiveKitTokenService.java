package com.whylog.server.domain.meeting.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LiveKitTokenService {

    private final String liveKitApiKey;
    private final String liveKitApiSecret;

    public LiveKitTokenService(
            @Value("${livekit.api-key}") @NotBlank String liveKitApiKey,
            @Value("${livekit.api-secret}") @NotBlank String liveKitApiSecret
    ) {
        this.liveKitApiKey = liveKitApiKey;
        this.liveKitApiSecret = liveKitApiSecret;
    }

    public String createJoinToken(String identity, String name, String roomName) {
        Map<String, Object> videoGrant = new LinkedHashMap<>();
        videoGrant.put("roomJoin", true);
        videoGrant.put("room", roomName);
        videoGrant.put("canPublish", true);
        videoGrant.put("canSubscribe", true);
        videoGrant.put("canPublishData", true);

        return createToken(identity, name, videoGrant);
    }

    public String createRoomRecordToken(String identity, String roomName) {
        Map<String, Object> videoGrant = new LinkedHashMap<>();
        videoGrant.put("roomRecord", true);
        videoGrant.put("room", roomName);

        return createToken(identity, "recording-bot", videoGrant);
    }

    public String createRoomCreateToken(String identity) {
        Map<String, Object> videoGrant = new LinkedHashMap<>();
        videoGrant.put("roomCreate", true);

        return createToken(identity, "room-admin", videoGrant);
    }

    private String createToken(String identity, String name, Map<String, Object> videoGrant) {
        return Jwts.builder()
                .setIssuer(liveKitApiKey)
                .setSubject(identity)
                .claim("name", name)
                .claim("video", videoGrant)
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(liveKitApiSecret.getBytes(StandardCharsets.UTF_8));
    }
}
