package com.sep.psp.service.impl;

import com.sep.psp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMfaCode(String to, String code) {
        log.info("📧 Sending MFA code to: {}", to);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("psp-system@no-reply.com");
        message.setTo(to);
        message.setSubject("Your PSP Access Code");
        message.setText("Your MFA verification code is: " + code + "\nValid for 5 minutes.");

        try {
            mailSender.send(message);
            log.info("MFA code sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send MFA code to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }
}