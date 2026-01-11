package com.sep.psp.security;

import com.sep.psp.entity.Role;
import com.sep.psp.entity.User;
import com.sep.psp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SuperAdminSeedConfig {

    @Value("${psp.superadmin.email}")
    private String adminEmail;

    @Value("${psp.superadmin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedSuperAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            User superAdmin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.SUPER_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);

            System.out.println("[PSP] Seeded SUPER_ADMIN user: " + adminEmail);
        };
    }
}
