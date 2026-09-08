package com.whylog.server.domain.user.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_404", "찾을 수 없는 유저입니다."),
    MEMBER_WITHDRAWAL_OWNER_HAS_MEMBERS(
            HttpStatus.CONFLICT, "MEMBER_409", "다른 팀원이 남아 있는 팀의 소유권을 이전한 뒤 탈퇴해 주세요."),
    MEMBER_WITHDRAWAL_NOT_ALLOWED(HttpStatus.CONFLICT, "MEMBER_409_2", "탈퇴를 요청할 수 없는 계정 상태입니다."),
    MEMBER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MEMBER_400", "현재 비밀번호가 올바르지 않습니다."),
    MEMBER_PROFILE_IMAGE_INVALID(
            HttpStatus.BAD_REQUEST, "MEMBER_400_2", "JPEG, PNG, WebP 이미지 파일만 업로드할 수 있습니다."),
    MEMBER_PROFILE_IMAGE_TOO_LARGE(
            HttpStatus.PAYLOAD_TOO_LARGE, "MEMBER_413", "프로필 이미지는 5MB 이하만 업로드할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder().isSuccess(false).code(code).message(message).build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
