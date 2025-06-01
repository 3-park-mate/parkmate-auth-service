package com.parkmate.authservice.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendAccountLockedAlert(String email, String name) {

        String subject = MailMessageConstants.ACCOUNT_LOCKED_SUBJECT;
        String body = String.format(MailMessageConstants.ACCOUNT_LOCKED_BODY_TEMPLATE, name);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendVerificationCode(String email, String code) {

        String subject = MailMessageConstants.VERIFICATION_CODE_SUBJECT;
        String body = String.format(MailMessageConstants.VERIFICATION_CODE_BODY_TEMPLATE, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}