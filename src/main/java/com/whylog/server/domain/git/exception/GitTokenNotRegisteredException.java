package com.whylog.server.domain.git.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class GitTokenNotRegisteredException extends GeneralException {

    public GitTokenNotRegisteredException() {
        super(GitErrorCode.GITHUB_TOKEN_NOT_REGISTERED);
    }

}
