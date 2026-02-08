package com.sep.psp.service;

import com.sep.psp.dto.auth.AuthResponse;
import com.sep.psp.dto.auth.LoginRequest;
import com.sep.psp.dto.auth.MfaVerificationRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);
    AuthResponse verifyMfa(MfaVerificationRequest request);

}
