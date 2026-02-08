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

        if (userRepository.existsByEmail(email)) {
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
        log.info("New user registered: {}", email);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: User with email {} not found", email);
                    return new UnauthorizedException(INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password attempt for user: {}", email);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            log.warn("Login attempt for disabled user: {}", email);
            throw new BadRequestException("User is disabled.");
        }

        log.info("User logged in successfully: {}", email);
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .build();
    }

}
