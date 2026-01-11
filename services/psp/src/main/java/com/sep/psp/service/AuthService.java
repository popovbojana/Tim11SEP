package com.sep.psp.service;

import com.sep.psp.dto.auth.AuthResponse;
import com.sep.psp.dto.auth.LoginRequest;
import com.sep.psp.entity.User;
import com.sep.psp.repository.UserRepository;
import com.sep.psp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("User is inactive");
        }

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.builder().token(token).build();
    }

}
