package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.exception.MemberNotFoundException;
import com.whylog.server.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberUseCase {

    private final MemberRepository memberRepository;

    // id로 member 조회
    public Member findMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(MemberNotFoundException::new);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
    }
}
