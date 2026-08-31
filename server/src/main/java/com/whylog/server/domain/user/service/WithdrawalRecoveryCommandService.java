package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.exception.AuthErrorCode;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WithdrawalRecoveryCommandService {

    private final MemberRepository memberRepository;
    private final WithdrawalRecoveryChallengeService withdrawalRecoveryChallengeService;

    public Member verify(AuthRequest.WithdrawalRecoveryVerifyDTO request) {
        Long memberId = request.getMemberId();
        if (memberId == null
                || !withdrawalRecoveryChallengeService.consume(memberId, request.getChallenge())) {
            throw new ErrorHandler(AuthErrorCode.WITHDRAWAL_RECOVERY_CHALLENGE_INVALID);
        }

        Member member =
                memberRepository
                        .findByIdForUpdate(memberId)
                        .orElseThrow(
                                () ->
                                        new ErrorHandler(
                                                AuthErrorCode
                                                        .WITHDRAWAL_RECOVERY_CHALLENGE_INVALID));
        if (!member.isWithdrawalGraceActive(LocalDateTime.now())) {
            throw new ErrorHandler(AuthErrorCode.WITHDRAWAL_RECOVERY_CHALLENGE_INVALID);
        }
        member.recoverWithdrawal();
        return member;
    }
}
