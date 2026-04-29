package com.whylog.server.global.external.fast.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class FastApiException extends GeneralException {

    public FastApiException(FastApiErrorCode errorCode) {
        super(errorCode);
    }

    public FastApiException(FastApiErrorCode errorCode, Throwable cause) {
        super(errorCode);
        initCause(cause);
    }
}
