package com.whylog.server.domain.user.repository;

import com.whylog.server.domain.user.entity.EmailVerificationCode;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationCodeRepository
        extends JpaRepository<EmailVerificationCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from EmailVerificationCode c where c.memberId = :memberId")
    Optional<EmailVerificationCode> findByMemberIdForUpdate(@Param("memberId") Long memberId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            delete from EmailVerificationCode c
            where c.memberId = :memberId
              and c.codeHmac = :codeHmac
            """)
    int deleteIfCodeMatches(@Param("memberId") Long memberId, @Param("codeHmac") String codeHmac);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            update EmailVerificationCode c
            set c.codeHmac = :previousCodeHmac,
                c.expiresAt = :previousExpiresAt,
                c.lastIssuedAt = :previousLastIssuedAt,
                c.failedAttempts = :previousFailedAttempts
            where c.memberId = :memberId
              and c.codeHmac = :issuedCodeHmac
            """)
    int restorePreviousCodeIfIssuedCodeMatches(
            @Param("memberId") Long memberId,
            @Param("issuedCodeHmac") String issuedCodeHmac,
            @Param("previousCodeHmac") String previousCodeHmac,
            @Param("previousExpiresAt") LocalDateTime previousExpiresAt,
            @Param("previousLastIssuedAt") LocalDateTime previousLastIssuedAt,
            @Param("previousFailedAttempts") int previousFailedAttempts);
}
