package com.whylog.server.global.external.email;

public class EmailVerificationSendException extends RuntimeException {

    public EmailVerificationSendException(String message) {
        super(message);
    }

    public EmailVerificationSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
