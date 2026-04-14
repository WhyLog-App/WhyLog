package com.whylog.server.domain.team.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements BaseErrorCode {

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_404", "존재하지 않는 팀입니다."),

    // 400 Bad Request
    TEAM_NAME_LENGTH(HttpStatus.BAD_REQUEST, "TEAM_400", "팀명 길이는 50글자 미만이어야 합니다."),

    // 409 Conflict
    TEAM_MEMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "TEAM_409", "이미 팀에 속한 사용자입니다."),

    // 422 Unprocessable Entity
    TEAM_NAME_ALREADY_EXISTS(HttpStatus.UNPROCESSABLE_ENTITY, "TEAM_420", "이미 존재하는 팀명입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
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
