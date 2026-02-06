package com.sep.psp.service;

import com.sep.psp.dto.auth.AuthResponse;
import com.sep.psp.dto.auth.LoginRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

}
