package com.sep.psp.service;

import com.sep.psp.dto.MerchantResponse;
import com.sep.psp.entity.Merchant;
import com.sep.psp.entity.PaymentMethod;
import com.sep.psp.repository.MerchantRepository;
import com.sep.psp.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final PaymentMethodRepository paymentMethodRepository;

    @Transactional
    public MerchantResponse create(String merchantKey) {
        if (merchantRepository.existsByMerchantKey(merchantKey)) {
            throw new IllegalArgumentException("Merchant already exists: " + merchantKey);
        }

        Merchant merchant = Merchant.builder()
                .merchantKey(merchantKey)
                .build();

        PaymentMethod defaultMethod = paymentMethodRepository.findByName("CARD")
                .orElseThrow(() -> new IllegalStateException("Default payment method CARD not found in database"));

        merchant.getActiveMethods().add(defaultMethod);

        return toResponse(merchantRepository.save(merchant));
    }

    @Transactional(readOnly = true)
    public Merchant getByMerchantKey(String merchantKey) {
        return merchantRepository.findByMerchantKey(merchantKey)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found: " + merchantKey));
    }

    @Transactional
    public Set<PaymentMethod> updateActiveMethods(String merchantKey, Set<String> methodNames) {
        if (methodNames == null || methodNames.isEmpty()) {
            throw new IllegalArgumentException("Merchant must have at least 1 active payment method.");
        }

        Merchant merchant = getByMerchantKey(merchantKey);

        Set<PaymentMethod> foundMethods = methodNames.stream()
                .map(name -> paymentMethodRepository.findByName(name.toUpperCase().trim())
                        .orElseThrow(() -> new IllegalArgumentException("Payment method not supported: " + name)))
                .collect(Collectors.toSet());

        if (foundMethods.isEmpty()) {
            throw new IllegalArgumentException("Merchant must have at least 1 active payment method.");
        }

        merchant.setActiveMethods(foundMethods);

        return merchantRepository.save(merchant).getActiveMethods();
    }

    @Transactional(readOnly = true)
    public Set<String> getActiveMethods(String merchantKey) {
        return getByMerchantKey(merchantKey).getActiveMethods()
                .stream()
                .map(PaymentMethod::getName)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public MerchantResponse getMerchant(String merchantKey) {
        return toResponse(getByMerchantKey(merchantKey));
    }

    public Set<MerchantResponse> getAll() {
        return merchantRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    private MerchantResponse toResponse(Merchant merchant) {
        return MerchantResponse.builder()
                .id(merchant.getId())
                .merchantKey(merchant.getMerchantKey())
                .activeMethods(getActiveMethods(merchant.getMerchantKey()))
                .build();
    }

}