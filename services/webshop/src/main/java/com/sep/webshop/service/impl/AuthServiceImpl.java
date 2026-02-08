package com.sep.webshop.service.impl;

import com.sep.webshop.dto.auth.AuthResponse;
import com.sep.webshop.dto.auth.LoginRequest;
import com.sep.webshop.dto.auth.RegisterRequest;
import com.sep.webshop.entity.User;
import com.sep.webshop.exception.BadRequestException;
import com.sep.webshop.exception.UnauthorizedException;
import com.sep.webshop.repository.UserRepository;
import com.sep.webshop.security.JwtService;
import com.sep.webshop.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final String INVALID_CREDENTIALS = "Invalid email or password.";

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Registration attempt for email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Registration failed — email already exists: {}", email);
            throw new BadRequestException("Email is already registered.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .active(true)
                .build();

        userRepository.save(user);
        log.info("User registered — email: {}, name: {} {}", email, user.getFirstName(), user.getLastName());

    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        log.info("Login attempt for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed — user not found: {}", email);
                    return new BadRequestException("Invalid credentials.");
                });

        if (!user.isActive()) {
            log.warn("Login failed — account disabled: {}", email);
            throw new BadRequestException("User is disabled.");
        }

        boolean ok = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!ok) {
            log.warn("Login failed — wrong password for: {}", email);
            throw new BadRequestException("Invalid credentials.");
        }
              
        String token = jwtService.generateToken(user.getEmail());
        log.info("Login successful — user: {}", email);

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}