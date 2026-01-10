package com.sep.psp.service;

import com.sep.psp.client.BankClient;
import com.sep.psp.dto.payment.InitPaymentRequest;
import com.sep.psp.dto.payment.InitPaymentResponse;
import com.sep.psp.dto.payment.PaymentResponse;
import com.sep.psp.dto.payment.StartPaymentResponse;
import com.sep.psp.entity.Payment;
import com.sep.psp.entity.PaymentStatus;
import com.sep.psp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BankClient bankClient;

    @Transactional
    public InitPaymentResponse initPayment(InitPaymentRequest request) {
        Payment payment = Payment.builder()
                .merchantKey(request.getMerchantKey())
                .merchantOrderId(request.getMerchantOrderId())
                .amount(request.getAmount())
                .status(PaymentStatus.CREATED)
                .createdAt(Instant.now())
                .successUrl(request.getSuccessUrl())
                .failedUrl(request.getFailedUrl())
                .errorUrl(request.getErrorUrl())
                .build();

        Payment saved = paymentRepository.save(payment);

        return InitPaymentResponse.builder()
                .paymentId(saved.getId())
                .redirectUrl("/checkout/" + saved.getId())
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .merchantKey(payment.getMerchantKey())
                .merchantOrderId(payment.getMerchantOrderId())
                .build();
    }

    @Transactional
    public StartPaymentResponse startCardPayment(Long paymentId) {
        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        var bankInit = bankClient.initBankPayment();

        if (bankInit == null || bankInit.getRedirectUrl() == null || bankInit.getRedirectUrl().isBlank()) {
            throw new IllegalStateException("Bank init did not return redirectUrl.");
        }

        return StartPaymentResponse.builder()
                .redirectUrl(bankInit.getRedirectUrl())
                .build();
    }
}
