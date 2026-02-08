package com.sep.webshop.service;

import com.sep.webshop.dto.auth.AuthResponse;
import com.sep.webshop.dto.auth.LoginRequest;
import com.sep.webshop.dto.auth.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);
    AuthResponse login(LoginRequest request);

}
