package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.repository.EmailVerificationCodeRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationCodeFailureService {

    private final EmailVerificationCodeRepository codeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeUndeliveredCode(Long memberId, String codeHmac) {
        codeRepository.deleteIfCodeMatches(memberId, codeHmac);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void restorePreviousCode(
            Long memberId, String issuedCodeHmac, PreviousCode previousCode) {
        codeRepository.restorePreviousCodeIfIssuedCodeMatches(
                memberId,
                issuedCodeHmac,
                previousCode.codeHmac(),
                previousCode.expiresAt(),
                previousCode.lastIssuedAt(),
                previousCode.failedAttempts());
    }

    public record PreviousCode(
            String codeHmac,
            LocalDateTime expiresAt,
            LocalDateTime lastIssuedAt,
            int failedAttempts) {}
}
