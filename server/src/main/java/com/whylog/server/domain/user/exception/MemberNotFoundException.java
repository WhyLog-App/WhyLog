package com.whylog.server.domain.user.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class MemberNotFoundException extends GeneralException {

    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }
}
