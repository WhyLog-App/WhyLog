package com.whylog.server.domain.decision.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class DecisionNotFoundException extends GeneralException {

    public DecisionNotFoundException() {
        super(DecisionErrorCode.DECISION_NOT_FOUND);
    }
}
