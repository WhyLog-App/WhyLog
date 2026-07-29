package com.whylog.server.global.external.fast.exception;

import com.whylog.server.global.apiPayload.code.BaseErrorCode;
import com.whylog.server.global.apiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FastApiErrorCode implements BaseErrorCode {

    FAST_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FAST_API_500_1", "FastAPI 요청에 실패했습니다."),
    FAST_API_RESPONSE_EMPTY(HttpStatus.INTERNAL_SERVER_ERROR, "FAST_API_500_2", "FastAPI 응답이 비어있습니다."),
    FAST_API_INVALID_REQUEST_BODY(HttpStatus.INTERNAL_SERVER_ERROR, "FAST_API_500_3", "FastAPI 요청 본문 형식이 올바르지 않습니다.");

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
