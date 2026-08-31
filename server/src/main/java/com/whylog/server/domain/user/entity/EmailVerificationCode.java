package com.whylog.server.domain.user.entity;

import com.whylog.server.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "email_verification_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerificationCode extends BaseEntity {

    private static final Duration VALID_DURATION = Duration.ofMinutes(30);
    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "code_hmac", length = 64, nullable = false)
    private String codeHmac;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_issued_at", nullable = false)
    private LocalDateTime lastIssuedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerificationCode(
            Long memberId,
            String codeHmac,
            LocalDateTime expiresAt,
            LocalDateTime lastIssuedAt,
            int failedAttempts) {
        this.memberId = memberId;
        this.codeHmac = codeHmac;
        this.expiresAt = expiresAt;
        this.lastIssuedAt = lastIssuedAt;
        this.failedAttempts = failedAttempts;
    }

    public static EmailVerificationCode issue(
            Long memberId, String codeHmac, LocalDateTime issuedAt) {
        return EmailVerificationCode.builder()
                .memberId(memberId)
                .codeHmac(codeHmac)
                .expiresAt(issuedAt.plus(VALID_DURATION))
                .lastIssuedAt(issuedAt)
                .failedAttempts(0)
                .build();
    }

    public void replace(String codeHmac, LocalDateTime issuedAt) {
        this.codeHmac = codeHmac;
        this.expiresAt = issuedAt.plus(VALID_DURATION);
        this.lastIssuedAt = issuedAt;
        this.failedAttempts = 0;
    }

    public boolean isExpiredAt(LocalDateTime at) {
        return !at.isBefore(expiresAt);
    }

    public boolean isInvalidated() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    public void recordFailure() {
        this.failedAttempts += 1;
    }
}
