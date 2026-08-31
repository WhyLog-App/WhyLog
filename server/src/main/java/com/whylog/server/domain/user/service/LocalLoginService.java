package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.dto.AuthResponse;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.domain.user.exception.AuthErrorCode;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private final MemberRepository memberRepository;
    private final AuthenticationService authenticationService;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationCommandService emailVerificationCommandService;
    private final WithdrawalRecoveryChallengeService withdrawalRecoveryChallengeService;

    @Transactional
    public AuthResponse.SignUpResponseDTO signUp(AuthRequest.SignUpDTO request) {
        String email = Member.canonicalizeEmail(request.getEmail());
        if (memberRepository.existsByEmail(email)) {
            throw new ErrorHandler(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member member = memberRepository.save(Member.create(request, encodedPassword, Role.USER));
        emailVerificationCommandService.issueForSignup(member);

        return AuthResponse.SignUpResponseDTO.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse.LoginResponseDTO login(AuthRequest.LoginDTO request) {
        String email = Member.canonicalizeEmail(request.getEmail());
        Member member =
                memberRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ErrorHandler(AuthErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new ErrorHandler(AuthErrorCode.LOGIN_FAILED);
        }

        if (member.isWithdrawalGraceActive(LocalDateTime.now())) {
            String challenge = withdrawalRecoveryChallengeService.issue(member.getId());
            return AuthResponse.LoginResponseDTO.recoveryRequired(
                    member.getId(),
                    member.getEmail(),
                    member.getRole().getRoleName(),
                    challenge,
                    member.getPurgeAt());
        }

        if (member.getAccountStatus() == AccountStatus.UNVERIFIED) {
            return AuthResponse.LoginResponseDTO.emailVerificationRequired(
                    member.getId(), member.getEmail(), member.getRole().getRoleName());
        }

        if (!member.getAccountStatus().canUseNormalService()) {
            throw new ErrorHandler(AuthErrorCode.LOGIN_FAILED);
        }

        return authenticationService.generateLoginResponse(member);
    }
}
