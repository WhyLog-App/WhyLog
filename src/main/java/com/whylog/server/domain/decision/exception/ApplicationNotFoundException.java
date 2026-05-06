package com.whylog.server.domain.decision.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class ApplicationNotFoundException extends GeneralException {

    public ApplicationNotFoundException() {
        super(DecisionErrorCode.APPLICATION_NOT_FOUND);
    }
}
