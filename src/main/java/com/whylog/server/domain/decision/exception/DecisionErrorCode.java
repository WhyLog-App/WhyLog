package com.whylog.server.domain.decision.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DecisionErrorCode implements BaseErrorCode {

    DECISION_NOT_FOUND(HttpStatus.NOT_FOUND, "DECISION_404", "존재하지 않는 결정사항입니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "APPLICATION_404", "존재하지 않는 적용사항입니다."),
    APPLICATION_COMMIT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "APPLICATION_COMMIT_409", "이미 연결된 커밋입니다."),
    APPLICATION_COMMIT_NOT_CONNECTED(HttpStatus.NOT_FOUND, "APPLICATION_COMMIT_404", "연결되지 않은 커밋입니다."),
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
