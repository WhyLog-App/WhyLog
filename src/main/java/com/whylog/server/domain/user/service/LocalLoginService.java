package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.AuthRequest;
import com.whylog.server.domain.user.dto.AuthResponse;
import com.whylog.server.domain.user.exception.AuthErrorStatus;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.enums.Role;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
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

    @Transactional
    public AuthResponse.LoginResponseDTO signUp(AuthRequest.SignUpDTO request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ErrorHandler(AuthErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        Member member = memberRepository.save(Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build());

        return authenticationService.generateLoginResponse(member);
    }

    @Transactional
    public AuthResponse.LoginResponseDTO login(AuthRequest.LoginDTO request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ErrorHandler(AuthErrorStatus.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new ErrorHandler(AuthErrorStatus.LOGIN_FAILED);
        }

        return authenticationService.generateLoginResponse(member);
    }
}
