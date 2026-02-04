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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

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
        return paymentMethodRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public PaymentMethodResponse add(PaymentMethodRequest methodRequest) {
        String name = methodRequest.getName().toUpperCase().trim();
        if (paymentMethodRepository.existsByName(name)) {
            throw new BadRequestException(String.format(ALREADY_EXISTS_MSG, name));
        }
        PaymentMethod newMethod = PaymentMethod.builder()
                .name(name)
                .serviceName(methodRequest.getServiceName())
                .build();
        return toResponse(paymentMethodRepository.save(newMethod));
    }

    @Override
    @Transactional
    public void remove(Long id) {
        PaymentMethod method = getById(id);

        if (paymentMethodRepository.count() <= 1) {
            throw new BadRequestException("Cannot delete the last payment method. The PSP system must maintain " +
                    "at least one available method.");
        }

        String problematicMerchants = merchantRepository.findAll().stream()
                .filter(merchant -> merchant.getActiveMethods().size() == 1 &&
                        merchant.getActiveMethods().contains(method))
                .map(Merchant::getMerchantKey)
                .collect(Collectors.joining(", "));

        if (!problematicMerchants.isEmpty()) {
            throw new BadRequestException("Cannot delete this method. It is the only active payment method for " +
                    "the following merchants: [" + problematicMerchants + "]. " +
                    "Please assign them an alternative method first.");
        }

        paymentMethodRepository.deleteRelationFromJoinTable(id);
        paymentMethodRepository.delete(method);
    }

    @Override
    @Transactional
    public PaymentMethodResponse update(Long id, PaymentMethodRequest methodRequest) {
        PaymentMethod method = getById(id);

        String newName = methodRequest.getName().toUpperCase().trim();
        if (!method.getName().equals(newName) && paymentMethodRepository.existsByName(newName)) {
            throw new BadRequestException(String.format(ALREADY_EXISTS_MSG, newName));
        }

        method.setName(newName);
        method.setServiceName(methodRequest.getServiceName());

        return toResponse(paymentMethodRepository.save(method));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse findById(Long id) {
        return toResponse(getById(id));
    }

    private PaymentMethod getById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .name(paymentMethod.getName())
                .serviceName(paymentMethod.getServiceName())
                .build();
    }
}