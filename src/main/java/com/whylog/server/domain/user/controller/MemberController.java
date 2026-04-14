package com.whylog.server.domain.user.controller;

import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.service.MemberCommandService;
import com.whylog.server.global.apiPayload.ApiResponse;
import com.whylog.server.global.auth.annotation.CurrentMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "멤버 관련 API")
public class MemberController {

    private final MemberCommandService memberCommandService;

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "멤버 프로필 이미지 업로드 API", description = "현재 로그인한 멤버의 프로필 이미지를 업로드합니다.")
    public ApiResponse<MemberResponse.ProfileImageUploadResponseDTO> uploadProfileImage(
            @CurrentMember Long memberId,
            @RequestPart("image") MultipartFile image
    ) {
        return ApiResponse.onSuccess(memberCommandService.uploadProfileImage(memberId, image));
    }

}
