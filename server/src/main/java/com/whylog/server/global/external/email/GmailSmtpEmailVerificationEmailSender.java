package com.whylog.server.global.external.email;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GmailSmtpEmailVerificationEmailSender implements EmailVerificationEmailSender {

    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 587;
    private static final String SUBJECT = "WhyLog 이메일 인증";
    private static final String ENCODING = "UTF-8";
    private static final String CONNECT_TIMEOUT_MILLIS = "3000";
    private static final String TIMEOUT_MILLIS = "5000";
    private static final String WRITE_TIMEOUT_MILLIS = "5000";

    private final JavaMailSenderImpl mailSender;
    private final String fromEmail;

    public GmailSmtpEmailVerificationEmailSender(
            @Value("${GMAIL_SMTP_USERNAME}") String username,
            @Value("${GMAIL_SMTP_APP_PASSWORD}") String appPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(appPassword)) {
            throw new IllegalStateException("Gmail SMTP 이메일 발송 설정이 비어 있습니다.");
        }
        this.fromEmail = username;
        this.mailSender = mailSender(username, appPassword);
    }

    @Override
    public void send(EmailVerificationEmailMessage message) {
        try {
            mailSender.send(mailMessage(message));
        } catch (MailException exception) {
            throw new EmailVerificationSendException("이메일 인증 코드 발송에 실패했습니다.", exception);
        }
    }

    private JavaMailSenderImpl mailSender(String username, String appPassword) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(HOST);
        sender.setPort(PORT);
        sender.setUsername(username);
        sender.setPassword(appPassword);
        sender.setDefaultEncoding(ENCODING);
        sender.setJavaMailProperties(mailProperties());
        return sender;
    }

    private Properties mailProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.connectiontimeout", CONNECT_TIMEOUT_MILLIS);
        properties.put("mail.smtp.timeout", TIMEOUT_MILLIS);
        properties.put("mail.smtp.writetimeout", WRITE_TIMEOUT_MILLIS);
        return properties;
    }

    private SimpleMailMessage mailMessage(EmailVerificationEmailMessage message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(message.recipientEmail());
        mailMessage.setSubject(SUBJECT);
        mailMessage.setText("WhyLog 이메일 인증 코드: " + message.code());
        return mailMessage;
    }
}
