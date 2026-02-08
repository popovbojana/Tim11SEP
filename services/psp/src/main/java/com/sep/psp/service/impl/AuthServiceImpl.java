package com.sep.psp.service.impl;

import com.sep.psp.dto.auth.AuthResponse;
import com.sep.psp.dto.auth.LoginRequest;
import com.sep.psp.dto.auth.MfaVerificationRequest; // Dodaj import
import com.sep.psp.entity.User;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.exception.NotFoundException;
import com.sep.psp.exception.UnauthorizedException;
import com.sep.psp.repository.UserRepository;
import com.sep.psp.security.JwtService;
import com.sep.psp.service.AuthService;
import com.sep.psp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private static final String INVALID_CREDENTIALS = "Invalid email or password.";

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for email: {}", request.getEmail());
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new BadRequestException("Your account is deactivated.");
        }

        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        user.setMfaCode(code);
        user.setMfaExpiry(Instant.now().plus(Duration.ofMinutes(5)));
        userRepository.save(user);

        emailService.sendMfaCode(user.getEmail(), code);

        log.info("MFA process started for user: {}", user.getEmail());
        return AuthResponse.builder()
                .mfaRequired(true)
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verifyMfa(MfaVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found."));

        if (user.getMfaCode() == null || !user.getMfaCode().equals(request.getCode())) {
            log.warn("Invalid MFA code entered for user: {}", request.getEmail());
            throw new UnauthorizedException("Invalid MFA code.");
        }

        if (user.getMfaExpiry().isBefore(Instant.now())) {
            log.warn("Expired MFA code for user: {}", request.getEmail());
            throw new UnauthorizedException("MFA code expired.");
        }

        user.setMfaCode(null);
        user.setMfaExpiry(null);
        userRepository.save(user);

        log.info("MFA verification successful for user: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build();
    }
}