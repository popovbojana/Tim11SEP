package com.sep.psp.service.impl;

import com.sep.psp.dto.merchant.MerchantCreateRequest;
import com.sep.psp.dto.merchant.MerchantResponse;
import com.sep.psp.dto.merchant.MerchantUpdateRequest;
import com.sep.psp.entity.Merchant;
import com.sep.psp.entity.PaymentMethod;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.exception.NotFoundException;
import com.sep.psp.repository.MerchantRepository;
import com.sep.psp.repository.PaymentMethodRepository;
import com.sep.psp.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MerchantResponse create(MerchantCreateRequest request) {
        log.info("📨 Creating merchant — key: {}, name: {}", request.getMerchantKey(), request.getFullName());

        if (merchantRepository.existsByMerchantKey(request.getMerchantKey())) {
            log.warn("❌ Merchant already exists — key: {}", request.getMerchantKey());
            throw new BadRequestException("Merchant with key: " + request.getMerchantKey() + " already exists.");
        }

        String rawPassword = generateSecurePassword();
        Set<PaymentMethod> selectedMethods = mapMethods(request.getMethods());

        Merchant merchant = Merchant.builder()
                .merchantKey(request.getMerchantKey())
                .merchantPassword(passwordEncoder.encode(rawPassword))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .successUrl(request.getSuccessUrl())
                .failedUrl(request.getFailedUrl())
                .errorUrl(request.getErrorUrl())
                .serviceName(request.getServiceName())
                .bankAccount(request.getBankAccount())
                .activeMethods(selectedMethods)
                .build();

        Merchant saved = merchantRepository.save(merchant);
        log.info("✅ Merchant created — ID: {}, key: {}, methods: {}", saved.getId(), saved.getMerchantKey(), request.getMethods());

        saveCredentialsToFile(saved.getMerchantKey(), rawPassword, saved.getFullName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public MerchantResponse update(String merchantKey, MerchantUpdateRequest request) {
        log.info("⚙️ Updating merchant: {}", merchantKey);

        Merchant merchant = getByMerchantKey(merchantKey);

        merchant.setFullName(request.getFullName());
        merchant.setEmail(request.getEmail());
        merchant.setSuccessUrl(request.getSuccessUrl());
        merchant.setFailedUrl(request.getFailedUrl());
        merchant.setErrorUrl(request.getErrorUrl());
        merchant.setServiceName(request.getServiceName());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setActiveMethods(mapMethods(request.getMethods()));

        Merchant saved = merchantRepository.save(merchant);
        log.info("✅ Merchant updated — key: {}, new methods: {}", merchantKey, request.getMethods());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Merchant getByMerchantKey(String merchantKey) {
        log.info("🔍 Looking up merchant by key: {}", merchantKey);
        return merchantRepository.findByMerchantKey(merchantKey)
                .orElseThrow(() -> {
                    log.warn("❌ Merchant NOT FOUND — key: {}", merchantKey);
                    return new NotFoundException("Merchant with key: " + merchantKey + " not found.");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(String merchantKey) {
        log.info("🔍 Getting merchant response for key: {}", merchantKey);
        return toResponse(getByMerchantKey(merchantKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<MerchantResponse> getAll() {
        log.info("📋 Fetching all merchants");
        Set<MerchantResponse> merchants = merchantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
        log.info("✅ Found {} merchants", merchants.size());
        return merchants;
    }

    @Override
    @Transactional
    public void remove(Long id) {
        log.info("🗑️ Removing merchant with ID: {}", id);

        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("❌ Merchant NOT FOUND — ID: {}", id);
                    return new NotFoundException("Merchant with ID " + id + " not found.");
                });

        merchant.getActiveMethods().clear();
        merchantRepository.delete(merchant);
        log.info("✅ Merchant removed — ID: {}, key: {}", id, merchant.getMerchantKey());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getActiveMethods(String merchantKey) {
        log.info("🔍 Fetching active methods for merchant: {}", merchantKey);
        Merchant merchant = getByMerchantKey(merchantKey);
        Set<String> methods = merchant.getActiveMethods().stream()
                .map(PaymentMethod::getName)
                .collect(Collectors.toSet());
        log.info("✅ Active methods for {}: {}", merchantKey, methods);
        return methods;
    }

    private Set<PaymentMethod> mapMethods(Set<String> methodNames) {
        if (methodNames == null || methodNames.isEmpty()) {
            log.warn("⚠️ No payment methods provided");
            throw new BadRequestException("At least one payment method must be selected.");
        }
        return methodNames.stream()
                .map(name -> paymentMethodRepository.findByName(name.toUpperCase().trim())
                        .orElseThrow(() -> {
                            log.warn("❌ Unsupported payment method: {}", name);
                            return new BadRequestException("Method not supported: " + name);
                        }))
                .collect(Collectors.toSet());
    }

    private MerchantResponse toResponse(Merchant merchant) {
        Set<String> methodNames = merchant.getActiveMethods().stream()
                .map(PaymentMethod::getName)
                .collect(Collectors.toSet());

        return MerchantResponse.builder()
                .id(merchant.getId())
                .merchantKey(merchant.getMerchantKey())
                .fullName(merchant.getFullName())
                .email(merchant.getEmail())
                .successUrl(merchant.getSuccessUrl())
                .failedUrl(merchant.getFailedUrl())
                .errorUrl(merchant.getErrorUrl())
                .serviceName(merchant.getServiceName())
                .bankAccount(merchant.getBankAccount())
                .activeMethods(methodNames)
                .build();
    }

    private String generateSecurePassword() {
        log.info("🔐 Generating secure merchant password");
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void saveCredentialsToFile(String key, String rawPassword, String merchantName) {
        String fileName = "merchants_credentials.txt";
        log.info("💾 Saving credentials to file for merchant: {}", merchantName);

        try (FileWriter fw = new FileWriter(fileName, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("--------------------------------------------------");
            pw.println("Merchant: " + merchantName);
            pw.println("Key:      " + key);
            pw.println("Password: " + rawPassword);
            pw.println("Generated: " + java.time.LocalDateTime.now());
            pw.println("--------------------------------------------------");
            log.info("✅ Credentials saved for merchant: {}", key);
        } catch (IOException e) {
            log.error("❌ Failed to save credentials for merchant {}: {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to save merchant credentials to file", e);
        }
    }
}