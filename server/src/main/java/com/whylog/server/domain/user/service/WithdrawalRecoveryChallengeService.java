package com.whylog.server.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WithdrawalRecoveryChallengeService {

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String KEY_PREFIX = "withdrawal-recovery:";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public String issue(Long memberId) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redisTemplate.opsForValue().set(key(memberId, rawChallenge), memberId.toString(), TTL);
        return rawChallenge;
    }

    public boolean consume(Long memberId, String rawChallenge) {
        String storedMemberId =
                redisTemplate.opsForValue().getAndDelete(key(memberId, rawChallenge));
        return memberId.toString().equals(storedMemberId);
    }

    private String key(Long memberId, String rawChallenge) {
        return KEY_PREFIX + memberId + ":" + sha256Hex(rawChallenge);
    }

    private String sha256Hex(String rawChallenge) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(rawChallenge.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
