package com.parkmate.authservice.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_BOUND = (int) Math.pow(10, VERIFICATION_CODE_LENGTH);

    public void sendAccountLockEmail(String email, String name) {

        String subject = MailMessageConstants.ACCOUNT_LOCKED_SUBJECT;
        String body = String.format(MailMessageConstants.ACCOUNT_LOCKED_BODY_TEMPLATE, name);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendVerificationEmail(String email, String code) {

        String subject = MailMessageConstants.VERIFICATION_CODE_SUBJECT;
        String body = String.format(MailMessageConstants.VERIFICATION_CODE_BODY_TEMPLATE, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public String generateVerificationCode() {
        int code = new Random().nextInt(VERIFICATION_CODE_BOUND);
        return String.format("%0" + VERIFICATION_CODE_LENGTH + "d", code);
    }
}