package com.whylog.server.global.external.email;

public interface EmailVerificationEmailSender {

    void send(EmailVerificationEmailMessage message);
}
