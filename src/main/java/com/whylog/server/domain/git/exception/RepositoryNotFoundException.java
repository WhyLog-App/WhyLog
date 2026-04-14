package com.whylog.server.domain.git.exception;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class RepositoryNotFoundException extends GeneralException {

    public RepositoryNotFoundException() {super(GitErrorCode.REPOSITORY_NOT_FOUND);}

}
