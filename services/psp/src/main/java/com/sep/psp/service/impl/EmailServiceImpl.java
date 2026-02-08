package com.sep.psp.service.impl;

import com.sep.psp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendMfaCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("psp-system@no-reply.com");
        message.setTo(to);
        message.setSubject("Your PSP Access Code");
        message.setText("Your MFA verification code is: " + code + "\nValid for 5 minutes.");
        mailSender.send(message);
    }
}