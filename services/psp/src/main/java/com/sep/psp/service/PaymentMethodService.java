package com.sep.psp.service;

import com.sep.psp.dto.PaymentMethodRequest;
import com.sep.psp.dto.PaymentMethodResponse;
import com.sep.psp.entity.PaymentMethod;
import com.sep.psp.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public Set<PaymentMethodResponse> findAll() {
        return paymentMethodRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    @Transactional
    public PaymentMethodResponse add(PaymentMethodRequest methodRequest) {
        if (paymentMethodRepository.existsByName(methodRequest.getName().toUpperCase())) {
            throw new IllegalArgumentException("Payment method already exists: " + methodRequest.getName());
        }
        PaymentMethod newMethod = PaymentMethod.builder()
                .name(methodRequest.getName().toUpperCase().trim())
                .serviceName(methodRequest.getServiceName())
                .build();
        return toResponse(paymentMethodRepository.save(newMethod));
    }

    @Transactional
    public void remove(Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method with ID " + id + " not found."));

        paymentMethodRepository.deleteRelationFromJoinTable(id);
        paymentMethodRepository.delete(method);
    }

    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest methodRequest) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found."));

        String newName = methodRequest.getName().toUpperCase().trim();
        if (!method.getName().equals(newName) && paymentMethodRepository.existsByName(newName)) {
            throw new IllegalArgumentException("Payment method with name " + newName + " already exists.");
        }

        method.setName(newName);
        method.setServiceName(methodRequest.getServiceName());

        return toResponse(paymentMethodRepository.save(method));
    }

    public PaymentMethodResponse findById(Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment method not found."));
        return toResponse(method);
    }

    private PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .serviceName(paymentMethod.getServiceName())
                .build();
    }
}