package com.whylog.server.global.external.s3;

import com.whylog.server.global.apiPayload.exception.GeneralException;

public class S3Exception extends GeneralException {

    public S3Exception(S3ErrorCode errorCode) {
        super(errorCode);
    }
}
