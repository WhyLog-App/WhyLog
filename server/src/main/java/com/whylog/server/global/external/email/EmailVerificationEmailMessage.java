package com.whylog.server.global.external.email;

public record EmailVerificationEmailMessage(String recipientEmail, String code) {

    @Override
    public String toString() {
        return "EmailVerificationEmailMessage[recipientEmail="
                + recipientEmail
                + ", code=[redacted]]";
    }
}
