package com.whylog.server.domain.user.service;

import com.whylog.server.domain.meeting.socket.MeetingSocketRoomService;
import com.whylog.server.domain.team.enums.TeamRole;
import com.whylog.server.domain.team.repository.TeamMemberRepository;
import com.whylog.server.domain.team.repository.TeamRepository;
import com.whylog.server.domain.user.dto.MemberRequest;
import com.whylog.server.domain.user.dto.MemberResponse;
import com.whylog.server.domain.user.entity.Member;
import com.whylog.server.domain.user.exception.MemberErrorCode;
import com.whylog.server.domain.user.repository.MemberRepository;
import com.whylog.server.global.apiPayload.exception.handler.ErrorHandler;
import com.whylog.server.global.auth.jwt.application.TokenService;
import com.whylog.server.global.external.s3.ImageType;
import com.whylog.server.global.external.s3.S3Client;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MemberCommandService {

    private static final long MAX_PROFILE_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_PROFILE_IMAGE_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final MemberRepository memberRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final TokenService tokenService;
    private final MeetingSocketRoomService meetingSocketRoomService;
    private final S3Client s3Client;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    public MemberResponse.MemberUpdateResponseDTO updateName(
            Long memberId, MemberRequest.NameUpdateDTO request) {
        Member member = findActiveMember(memberId);
        member.updateName(request.getName());
        return updateResponse(member);
    }

    public MemberResponse.ProfileVisibilityUpdateResponseDTO updateProfileVisibility(
            Long memberId, MemberRequest.ProfileVisibilityUpdateDTO request) {
        Member member = findActiveMember(memberId);
        member.updateProfileVisibility(request.getProfileVisibility());
        return MemberResponse.ProfileVisibilityUpdateResponseDTO.builder()
                .profileVisibility(member.getProfileVisibility())
                .build();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public MemberResponse.ProfileImageUploadResponseDTO uploadProfileImage(
            Long memberId, MultipartFile image) {
        validateProfileImage(image);
        String imageKey = s3Client.uploadFile(image, ImageType.MEMBER_PROFILE);
        try {
            return transactionTemplate.execute(status -> replaceProfileImage(memberId, imageKey));
        } catch (RuntimeException exception) {
            deleteUploadedImageAfterFailure(imageKey, exception);
            throw exception;
        }
    }

    public MemberResponse.MemberUpdateResponseDTO removeProfileImage(Long memberId) {
        Member member = findActiveMember(memberId);
        String previousImage = member.getProfileImage();
        member.removeProfileImage();
        scheduleAfterCommit(() -> deleteOldProfileImageBestEffort(previousImage));
        return updateResponse(member);
    }

    @Transactional(readOnly = true)
    public void verifyCurrentPassword(
            Long memberId, MemberRequest.CurrentPasswordVerifyDTO request) {
        Member member = findActiveMemberForCredentialCheck(memberId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
    }

    public MemberResponse.MemberUpdateResponseDTO changePassword(
            Long memberId, MemberRequest.PasswordChangeDTO request) {
        Member member = findActiveMember(memberId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
        member.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        tokenService.deleteRefreshTokenIfExists(memberId);
        return updateResponse(member);
    }

    private MemberResponse.ProfileImageUploadResponseDTO replaceProfileImage(
            Long memberId, String imageKey) {
        Member member = findActiveMember(memberId);
        String previousImage = member.getProfileImage();
        member.updateProfileImage(imageKey);
        scheduleAfterCommit(() -> deleteOldProfileImageBestEffort(previousImage));
        return MemberResponse.ProfileImageUploadResponseDTO.builder()
                .memberId(member.getId())
                .profileImageUrl(s3Client.getFileUrl(imageKey))
                .build();
    }

    private void deleteOldProfileImageBestEffort(String imageKey) {
        if (imageKey == null || imageKey.isBlank()) {
            return;
        }
        try {
            s3Client.deleteFile(imageKey);
        } catch (RuntimeException exception) {
            log.warn("Failed to delete old member profile image: {}", imageKey, exception);
        }
    }

    private void deleteUploadedImageAfterFailure(String imageKey, RuntimeException cause) {
        try {
            s3Client.deleteFile(imageKey);
        } catch (RuntimeException cleanupFailure) {
            cause.addSuppressed(cleanupFailure);
        }
    }

    private void validateProfileImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        if (image.getSize() > MAX_PROFILE_IMAGE_BYTES) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_PROFILE_IMAGE_TOO_LARGE);
        }
        String contentType = image.getContentType();
        if (!ALLOWED_PROFILE_IMAGE_TYPES.contains(contentType)
                || !hasExpectedImageSignature(image, contentType)) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_PROFILE_IMAGE_INVALID);
        }
    }

    private boolean hasExpectedImageSignature(MultipartFile image, String contentType) {
        try (InputStream inputStream = image.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            return switch (contentType) {
                case "image/jpeg" -> isJpeg(header);
                case "image/png" -> isPng(header);
                case "image/webp" -> isWebp(header);
                default -> false;
            };
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && (header[0] & 0xff) == 0xff
                && (header[1] & 0xff) == 0xd8
                && (header[2] & 0xff) == 0xff;
    }

    private boolean isPng(byte[] header) {
        int[] signature = {0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (header.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((header[index] & 0xff) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean isWebp(byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }

    private Member findActiveMember(Long memberId) {
        Member member =
                memberRepository
                        .findByIdForUpdate(memberId)
                        .orElseThrow(() -> new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND));
        if (!member.getAccountStatus().canUseNormalService()) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    private Member findActiveMemberForCredentialCheck(Long memberId) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(
                                () -> new ErrorHandler(MemberErrorCode.MEMBER_PASSWORD_MISMATCH));
        if (!member.getAccountStatus().canUseNormalService()) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
        return member;
    }

    private MemberResponse.MemberUpdateResponseDTO updateResponse(Member member) {
        return MemberResponse.MemberUpdateResponseDTO.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .profileImage(s3Client.getFileUrl(member.getProfileImage()))
                .build();
    }

    public void requestWithdrawal(Long memberId) {
        List<Long> ownedTeamIds =
                teamMemberRepository.findActiveOwnerTeamIdsByMemberId(memberId, TeamRole.OWNER);
        if (!ownedTeamIds.isEmpty()) {
            teamRepository.findAllByIdInForUpdate(ownedTeamIds);
        }

        Member member =
                memberRepository
                        .findByIdForUpdate(memberId)
                        .orElseThrow(() -> new ErrorHandler(MemberErrorCode.MEMBER_NOT_FOUND));
        if (!member.getAccountStatus().canUseNormalService()) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_WITHDRAWAL_NOT_ALLOWED);
        }
        if (!ownedTeamIds.isEmpty()
                && !teamMemberRepository
                        .findOwnedTeamIdsWithOtherActiveMembers(ownedTeamIds)
                        .isEmpty()) {
            throw new ErrorHandler(MemberErrorCode.MEMBER_WITHDRAWAL_OWNER_HAS_MEMBERS);
        }
        member.requestWithdrawal(LocalDateTime.now());
        tokenService.deleteRefreshTokenIfExists(memberId);
        scheduleAfterCommit(() -> meetingSocketRoomService.disconnectMemberSessions(memberId));
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
}
