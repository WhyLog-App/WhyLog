package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.entity.EmailVerificationCode;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.exception.AuthErrorCode;
import com.whylog.server.domain.user.repository.EmailVerificationCodeRepository;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.domain.user.service.EmailVerificationCodeFailureService.PreviousCode;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.external.email.EmailVerificationEmailMessage;
import com.whylog.server.global.external.email.EmailVerificationEmailSender;
import com.whylog.server.global.external.email.EmailVerificationSendException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailVerificationCommandService {

    private static final Duration ISSUE_COOLDOWN = Duration.ofSeconds(60);

    private final MemberRepository memberRepository;
    private final EmailVerificationCodeRepository codeRepository;
    private final EmailVerificationCodeCodec codeCodec;
    private final EmailVerificationEmailSender emailSender;
    private final EmailVerificationCodeFailureService codeFailureService;
    private final Clock clock = Clock.systemDefaultZone();

    public void issueForSignup(Member member) {
        issueForMember(
                member, now(), codeRepository.findByMemberIdForUpdate(member.getId()).orElse(null));
    }

    public void issue(AuthRequest.EmailVerificationIssueDTO request) {
        String email = Member.canonicalizeEmail(request.getEmail());
        Member member = memberRepository.findByEmailForUpdate(email).orElse(null);
        if (member == null || member.getAccountStatus() != AccountStatus.UNVERIFIED) {
            return;
        }

        LocalDateTime now = now();
        EmailVerificationCode currentCode =
                codeRepository.findByMemberIdForUpdate(member.getId()).orElse(null);
        if (currentCode != null
                && currentCode.getLastIssuedAt().plus(ISSUE_COOLDOWN).isAfter(now)) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_ISSUE_COOLDOWN);
        }

        issueForMember(member, now, currentCode);
    }

    @Transactional(noRollbackFor = ErrorHandler.class)
    public Member verify(AuthRequest.EmailVerificationVerifyDTO request) {
        String email = Member.canonicalizeEmail(request.getEmail());
        Member member = memberRepository.findByEmailForUpdate(email).orElse(null);
        if (member == null) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_CODE_INVALID);
        }
        if (member.getAccountStatus() != AccountStatus.UNVERIFIED) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_CODE_INVALID);
        }

        EmailVerificationCode verificationCode =
                codeRepository.findByMemberIdForUpdate(member.getId()).orElse(null);
        if (verificationCode == null || verificationCode.isInvalidated()) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_CODE_INVALID);
        }
        if (verificationCode.isExpiredAt(now())) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!codeCodec.matches(member.getId(), request.getCode(), verificationCode.getCodeHmac())) {
            verificationCode.recordFailure();
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_CODE_INVALID);
        }

        member.verifyEmail(now());
        codeRepository.delete(verificationCode);
        return member;
    }

    private void issueForMember(
            Member member, LocalDateTime now, EmailVerificationCode currentCode) {
        String rawCode = codeCodec.newCode();
        String codeHmac = codeCodec.hmac(member.getId(), rawCode);
        PreviousCode previousCode = snapshot(currentCode);
        if (currentCode == null) {
            codeRepository.save(EmailVerificationCode.issue(member.getId(), codeHmac, now));
        } else {
            currentCode.replace(codeHmac, now);
        }
        scheduleAfterCommit(
                () -> sendCode(member.getId(), member.getEmail(), rawCode, codeHmac, previousCode));
    }

    private void sendCode(
            Long memberId,
            String email,
            String rawCode,
            String codeHmac,
            PreviousCode previousCode) {
        try {
            emailSender.send(new EmailVerificationEmailMessage(email, rawCode));
        } catch (EmailVerificationSendException exception) {
            restoreCodeAfterDeliveryFailure(memberId, codeHmac, previousCode);
            throw new ErrorHandler(AuthErrorCode.EMAIL_VERIFICATION_DELIVERY_FAILED);
        }
    }

    private PreviousCode snapshot(EmailVerificationCode currentCode) {
        if (currentCode == null) {
            return null;
        }
        return new PreviousCode(
                currentCode.getCodeHmac(),
                currentCode.getExpiresAt(),
                currentCode.getLastIssuedAt(),
                currentCode.getFailedAttempts());
    }

    private void restoreCodeAfterDeliveryFailure(
            Long memberId, String codeHmac, PreviousCode previousCode) {
        if (previousCode == null) {
            codeFailureService.removeUndeliveredCode(memberId, codeHmac);
            return;
        }
        codeFailureService.restorePreviousCode(memberId, codeHmac, previousCode);
    }

    private void scheduleAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            task.run();
                        }
                    });
            return;
        }

        task.run();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
