package com.sep.psp.service.impl;

import com.sep.psp.dto.paymentMethod.PaymentMethodRequest;
import com.sep.psp.dto.paymentMethod.PaymentMethodResponse;
import com.sep.psp.entity.Merchant;
import com.sep.psp.entity.PaymentMethod;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.exception.NotFoundException;
import com.sep.psp.repository.MerchantRepository;
import com.sep.psp.repository.PaymentMethodRepository;
import com.sep.psp.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final MerchantRepository merchantRepository;

    private static final String NOT_FOUND_MSG = "Payment method with id %d not found.";
    private static final String ALREADY_EXISTS_MSG = "Payment method with name %s already exists.";

    @Override
    @Transactional(readOnly = true)
    public Set<PaymentMethodResponse> findAll() {
        log.info("Fetching all payment methods");
        Set<PaymentMethodResponse> methods = paymentMethodRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
        log.info("Found {} payment methods", methods.size());
        return methods;
    }

    @Override
    @Transactional
    public PaymentMethodResponse add(PaymentMethodRequest methodRequest) {
        String name = methodRequest.getName().toUpperCase().trim();
        log.info("Adding payment method: {}, service: {}", name, methodRequest.getServiceName());

        if (paymentMethodRepository.existsByName(name)) {
            log.warn("Payment method already exists: {}", name);
            throw new BadRequestException(String.format(ALREADY_EXISTS_MSG, name));
        }

        PaymentMethod newMethod = PaymentMethod.builder()
                .name(name)
                .serviceName(methodRequest.getServiceName().toUpperCase().trim())
                .build();

        PaymentMethod saved = paymentMethodRepository.save(newMethod);
        log.info("Payment method added — ID: {}, name: {}, service: {}", saved.getId(), name, saved.getServiceName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        log.info("Removing payment method with ID: {}", id);
        PaymentMethod method = getById(id);

        if (paymentMethodRepository.count() <= 1) {
            log.warn("Cannot delete — this is the last payment method");
            throw new BadRequestException("Cannot delete the last payment method. The PSP system must maintain " +
                    "at least one available method.");
        }

        String problematicMerchants = merchantRepository.findAll().stream()
                .filter(merchant -> merchant.getActiveMethods().size() == 1 &&
                        merchant.getActiveMethods().contains(method))
                .map(Merchant::getMerchantKey)
                .collect(Collectors.joining(", "));

        if (!problematicMerchants.isEmpty()) {
            log.warn("Cannot delete method '{}' — sole method for merchants: [{}]", method.getName(), problematicMerchants);
            throw new BadRequestException("Cannot delete this method. It is the only active payment method for " +
                    "the following merchants: [" + problematicMerchants + "]. " +
                    "Please assign them an alternative method first.");
        }

        paymentMethodRepository.deleteRelationFromJoinTable(id);
        paymentMethodRepository.delete(method);
        log.info("Payment method removed — ID: {}, name: {}", id, method.getName());
    }

    @Override
    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest methodRequest) {
        String newName = methodRequest.getName().toUpperCase().trim();
        log.info("Updating payment method ID: {} → name: {}, service: {}", id, newName, methodRequest.getServiceName());

        PaymentMethod method = getById(id);

        if (!method.getName().equals(newName) && paymentMethodRepository.existsByName(newName)) {
            log.warn("Cannot rename — method '{}' already exists", newName);
            throw new BadRequestException(String.format(ALREADY_EXISTS_MSG, newName));
        }

        method.setName(newName);
        method.setServiceName(methodRequest.getServiceName().toUpperCase().trim());

        PaymentMethod saved = paymentMethodRepository.save(method);
        log.info("Payment method updated — ID: {}, name: {}", saved.getId(), newName);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse findById(Long id) {
        log.info("Fetching payment method by ID: {}", id);
        return toResponse(getById(id));
    }

    private PaymentMethod getById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment method NOT FOUND — ID: {}", id);
                    return new NotFoundException(String.format(NOT_FOUND_MSG, id));
                });
    }

    private PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .serviceName(paymentMethod.getServiceName())
                .build();
    }
}