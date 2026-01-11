package com.sep.psp.service;

import com.sep.psp.client.BankClient;
import com.sep.psp.dto.payment.*;
import com.sep.psp.entity.Payment;
import com.sep.psp.entity.PaymentStatus;
import com.sep.psp.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

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
                .currency(request.getCurrency())
                .status(PaymentStatus.CREATED)
                .createdAt(Instant.now())
                .successUrl(request.getSuccessUrl())
                .failedUrl(request.getFailedUrl())
                .errorUrl(request.getErrorUrl())
                .stan(generateStan())
                .pspTimestamp(Instant.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        return InitPaymentResponse.builder()
                .paymentId(saved.getId())
                .redirectUrl("/checkout/" + saved.getId())
                .build();
    }

    @Transactional
    public StartPaymentResponse startCardPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.IN_PROGRESS
                && payment.getBankRedirectUrl() != null
                && !payment.getBankRedirectUrl().isBlank()) {
            return StartPaymentResponse.builder()
                    .redirectUrl(payment.getBankRedirectUrl())
                    .build();
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.ERROR) {
            throw new IllegalStateException("Payment is already finished: " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.IN_PROGRESS);
        payment.setPspTimestamp(Instant.now());
        paymentRepository.save(payment);

        InitBankPaymentRequest bankReq = InitBankPaymentRequest.builder()
                .pspPaymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp())
                .build();

        InitBankPaymentResponse bankInit = bankClient.initBankPayment(bankReq);

        if (bankInit == null || bankInit.getRedirectUrl() == null || bankInit.getRedirectUrl().isBlank()) {
            throw new IllegalStateException("Bank init did not return redirectUrl.");
        }

        payment.setBankPaymentId(bankInit.getBankPaymentId());
        payment.setBankRedirectUrl(bankInit.getRedirectUrl());
        paymentRepository.save(payment);

        return StartPaymentResponse.builder()
                .redirectUrl(bankInit.getRedirectUrl())
                .build();
    }

    private String generateStan() {
        return UUID.randomUUID().toString();
    }

    @Transactional()
    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));
        return PaymentResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .merchantKey(payment.getMerchantKey())
                .merchantOrderId(payment.getMerchantOrderId())
                .successUrl(payment.getSuccessUrl())
                .failedUrl(payment.getFailedUrl())
                .errorUrl(payment.getErrorUrl())
                .build();
    }

    public HttpHeaders finalize(Long bankPaymentId) {
        Payment payment = paymentRepository.findByBankPaymentId(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found for bankPaymentId=" + bankPaymentId));

        String targetUrl = resolveTargetUrl(payment);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Location", targetUrl);
        return headers;
    }

    private String resolveTargetUrl(Payment payment) {
        PaymentStatus status = payment.getStatus();

        if (status == PaymentStatus.SUCCESS) return payment.getSuccessUrl();
        if (status == PaymentStatus.FAILED) return payment.getFailedUrl();
        return payment.getErrorUrl();
    }
}
