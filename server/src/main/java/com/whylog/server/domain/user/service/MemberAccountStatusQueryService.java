package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.enums.AccountStatus;
import com.whylog.server.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAccountStatusQueryService {

    private final MemberRepository memberRepository;

    public boolean isActive(Long memberId) {
        return memberRepository.existsByIdAndAccountStatus(memberId, AccountStatus.ACTIVE);
    }
}
