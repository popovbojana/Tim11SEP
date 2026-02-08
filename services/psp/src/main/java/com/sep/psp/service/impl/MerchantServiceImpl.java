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

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MerchantResponse create(MerchantCreateRequest request) {
        if (merchantRepository.existsByMerchantKey(request.getMerchantKey())) {
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
        saveCredentialsToFile(saved.getMerchantKey(), rawPassword, saved.getFullName());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public MerchantResponse update(String merchantKey, MerchantUpdateRequest request) {
        Merchant merchant = getByMerchantKey(merchantKey);

        merchant.setFullName(request.getFullName());
        merchant.setEmail(request.getEmail());
        merchant.setSuccessUrl(request.getSuccessUrl());
        merchant.setFailedUrl(request.getFailedUrl());
        merchant.setErrorUrl(request.getErrorUrl());
        merchant.setServiceName(request.getServiceName());
        merchant.setBankAccount(request.getBankAccount());
        merchant.setActiveMethods(mapMethods(request.getMethods()));

        return toResponse(merchantRepository.save(merchant));
    }

    @Override
    @Transactional(readOnly = true)
    public Merchant getByMerchantKey(String merchantKey) {
        return merchantRepository.findByMerchantKey(merchantKey)
                .orElseThrow(() -> new NotFoundException("Merchant with key: " + merchantKey + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(String merchantKey) {
        return toResponse(getByMerchantKey(merchantKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<MerchantResponse> getAll() {
        return merchantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Merchant merchant = merchantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Merchant with ID " + id + " not found."));
        merchant.getActiveMethods().clear();
        merchantRepository.delete(merchant);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getActiveMethods(String merchantKey) {
        Merchant merchant = getByMerchantKey(merchantKey);
        return merchant.getActiveMethods().stream()
                .map(PaymentMethod::getName)
                .collect(Collectors.toSet());
    }

    private Set<PaymentMethod> mapMethods(Set<String> methodNames) {
        if (methodNames == null || methodNames.isEmpty()) {
            throw new BadRequestException("At least one payment method must be selected.");
        }
        return methodNames.stream()
                .map(name -> paymentMethodRepository.findByName(name.toUpperCase().trim())
                        .orElseThrow(() -> new BadRequestException("Method not supported: " + name)))
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
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void saveCredentialsToFile(String key, String rawPassword, String merchantName) {
        String fileName = "merchants_credentials.txt";
        try (FileWriter fw = new FileWriter(fileName, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("--------------------------------------------------");
            pw.println("Merchant: " + merchantName);
            pw.println("Key:      " + key);
            pw.println("Password: " + rawPassword);
            pw.println("Generated: " + java.time.LocalDateTime.now());
            pw.println("--------------------------------------------------");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save merchant credentials to file", e);
        }
    }
}