package com.whylog.server.domain.git.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GitErrorCode implements BaseErrorCode {
    REPOSITORY_NOT_FOUND(HttpStatus.NOT_FOUND, "GIT_404_1", "존재하지 않는 레포지토리입니다."),
    COMMIT_NOT_FOUND(HttpStatus.NOT_FOUND, "GIT_404_2", "존재하지 않는 커밋입니다."),
    INVALID_GITHUB_URL(HttpStatus.BAD_REQUEST, "GIT_400_1", "유효하지 않은 GitHub URL입니다."),
    GITHUB_TOKEN_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "GIT_400_2", "GitHub Access Token이 등록되지 않았습니다."),
    TOKEN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GIT_500_1", "GitHub 토큰 암호화 중 오류가 발생했습니다."),
    TOKEN_DECRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GIT_500_2", "GitHub 토큰 복호화 중 오류가 발생했습니다."),
    GITHUB_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "GIT_401_1", "GitHub Access Token이 만료되었습니다. 다시 인증해주세요."),
    GITHUB_API_ERROR(HttpStatus.BAD_GATEWAY, "GIT_502_1", "GitHub API 호출 중 오류가 발생했습니다."),;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}
