package com.sep.psp.config;

import com.sep.psp.entity.Role;
import com.sep.psp.entity.User;
import com.sep.psp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SuperAdminSeedConfig {

    @Value("${psp.superadmin.email}")
    private String adminEmail;

    @Bean
    public CommandLineRunner seedSuperAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.existsByEmail(adminEmail)) {
                return;
            }

            String randomPassword = UUID.randomUUID().toString().substring(0, 12);

            User superAdmin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(randomPassword))
                    .role(Role.SUPER_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);

            writeCredentialsToFile(randomPassword);
        };
    }

    private void writeCredentialsToFile(String password) {
        String fileName = "superadmin_credentials.txt";
        try (FileWriter fileWriter = new FileWriter(fileName);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {

            printWriter.println("--- PSP SUPER ADMIN CREDENTIALS ---");
            printWriter.println("Email: " + adminEmail);
            printWriter.println("Password: " + password);
            printWriter.println("Generated At: " + java.time.LocalDateTime.now());
            printWriter.println("-----------------------------------");

            System.out.println("[PSP] SuperAdmin created. Check " + fileName + " for password.");

        } catch (IOException e) {
            System.err.println("[PSP] Failed to write credentials to file: " + e.getMessage());
        }
    }
}