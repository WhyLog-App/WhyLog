package com.whylog.server.domain.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationCodeCodec {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String DOMAIN = "whylog:email-verification-code:v1";
    private static final int CODE_BOUND = 1_000_000;

    private final byte[] secret;
    private final SecureRandom secureRandom;

    @Autowired
    public EmailVerificationCodeCodec(@Value("${EMAIL_VERIFICATION_CODE_SECRET}") String secret) {
        this(secret, new SecureRandom());
    }

    EmailVerificationCodeCodec(String secret, SecureRandom secureRandom) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException(
                    "email verification HMAC secret must be at least 32 bytes");
        }
        this.secureRandom = secureRandom;
    }

    public String newCode() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(CODE_BOUND));
    }

    public String hmac(Long memberId, String code) {
        String payload = DOMAIN + "|memberId=" + memberId + "|code=" + code;
        byte[] result = hmac(payload);
        StringBuilder builder = new StringBuilder(result.length * 2);
        for (byte b : result) {
            builder.append(String.format(Locale.ROOT, "%02x", b));
        }
        return builder.toString();
    }

    public boolean matches(Long memberId, String code, String expectedHmac) {
        String actualHmac = hmac(memberId, code);
        return MessageDigest.isEqual(
                actualHmac.getBytes(StandardCharsets.UTF_8),
                expectedHmac.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] hmac(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("HMAC-SHA256 code generation failed", exception);
        }
    }
}
