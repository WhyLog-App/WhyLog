package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.exception.MemberNotFoundException;
import com.whylog.server.domain.user.repository.MemberRepository;
import java.util.List;

import com.whylog.server.global.external.s3.S3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUseCase {

    private final MemberRepository memberRepository;
    private final S3Client s3Client;

    // id로 member 조회
    public Member findMemberById(Long id){
        return memberRepository.findById(id)
                .orElseThrow(MemberNotFoundException::new);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }

    public List<Member> findMembersByIds(List<Long> memberIds) {
        return memberRepository.findAllById(memberIds);
    }

    public String getProfileImageUrl(Member member) {

        if(member == null)
            return null;

        // s3Client.getFileUrl에서 null 검사 해줘서 바로 리턴해줘도 됨
        return s3Client.getFileUrl(member.getProfileImage());
    }

}
