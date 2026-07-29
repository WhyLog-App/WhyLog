package com.whylog.server.domain.user.service;

import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.global.external.s3.ImageType;
import com.whylog.server.global.external.s3.S3Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberUseCase memberUseCase;
    private final S3Client s3Client;

    @Transactional
    public MemberResponse.ProfileImageUploadResponseDTO uploadProfileImage(Long memberId, MultipartFile image) {

        Member member = memberUseCase.findMemberById(memberId);
        String imageKey = s3Client.uploadFile(image, ImageType.MEMBER_PROFILE);
        member.updateProfileImage(imageKey);

        return MemberResponse.ProfileImageUploadResponseDTO.builder()
                .memberId(member.getId())
                .profileImageUrl(s3Client.getFileUrl(imageKey))
                .build();
    }
}
