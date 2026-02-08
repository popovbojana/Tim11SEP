package com.sep.psp.service;

public interface EmailService {

    void sendMfaCode(String to, String code);

}
