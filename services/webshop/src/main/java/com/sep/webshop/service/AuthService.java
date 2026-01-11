package com.sep.webshop.service;

import com.sep.webshop.dto.auth.AuthResponse;
import com.sep.webshop.dto.auth.LoginRequest;
import com.sep.webshop.dto.auth.RegisterRequest;
import com.sep.webshop.entity.User;
import com.sep.webshop.repository.UserRepository;
import com.sep.webshop.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .active(true)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!user.isActive()) {
            throw new IllegalArgumentException("User is disabled.");
        }

        boolean ok = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!ok) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
