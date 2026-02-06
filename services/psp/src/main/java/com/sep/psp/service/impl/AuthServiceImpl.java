package com.sep.psp.service.impl;

import com.sep.psp.dto.auth.AuthResponse;
import com.sep.psp.dto.auth.LoginRequest;
import com.sep.psp.entity.User;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.repository.UserRepository;
import com.sep.psp.security.JwtService;
import com.sep.psp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!user.isActive()) {
            throw new BadRequestException("Your account is deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.builder().token(token).build();
    }

}
