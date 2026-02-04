package com.sep.psp.service.impl;

import com.sep.psp.client.BankClient;
import com.sep.psp.client.WebshopClient;
import com.sep.psp.dto.payment.*;
import com.sep.psp.entity.Payment;
import com.sep.psp.entity.PaymentStatus;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.exception.NotFoundException;
import com.sep.psp.repository.PaymentRepository;
import com.sep.psp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BankClient bankClient;
    private final WebshopClient webshopClient;

    private static final String NOT_FOUND_MSG = "Payment with id: %d not found.";
    private static final String BANK_NOT_FOUND_MSG = "Payment with bank payment id: %d not found.";

    @Override
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

    @Override
    @Transactional
    public StartPaymentResponse startCardPayment(Long paymentId) {
        return startBankPayment(paymentId, "CARD");
    }

    @Override
    @Transactional
    public StartPaymentResponse startQrPayment(Long paymentId) {
        return startBankPayment(paymentId, "QR");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment payment = getById(id);

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

    @Override
    @Transactional
    public void handleBankCallback(BankCallbackRequest request) {
        if (request == null || request.getPspPaymentId() == null) {
            throw new BadRequestException("Missing pspPaymentId.");
        }

        Payment payment = getById(request.getPspPaymentId());

        if (isFinal(payment.getStatus())) {
            return;
        }

        if (request.getBankPaymentId() != null) {
            payment.setBankPaymentId(request.getBankPaymentId());
        }

        if (request.getStan() != null && !request.getStan().isBlank()) {
            payment.setStan(request.getStan());
        }

        if (request.getPspTimestamp() != null) {
            payment.setPspTimestamp(request.getPspTimestamp());
        }

        PaymentStatus mapped = mapBankStatus(request.getStatus());
        if (mapped != null) {
            payment.setStatus(mapped);
        }

        if (request.getGlobalTransactionId() != null && !request.getGlobalTransactionId().isBlank()) {
            payment.setGlobalTransactionId(request.getGlobalTransactionId());
        }

        if (request.getAcquirerTimestamp() != null) {
            payment.setAcquirerTimestamp(request.getAcquirerTimestamp());
        }

        paymentRepository.save(payment);

        if (isFinal(payment.getStatus())) {
            String method = "CARD";
            if (payment.getBankRedirectUrl() != null && payment.getBankRedirectUrl().toLowerCase().contains("/qr")) {
                method = "QR";
            }

            webshopClient.sendPaymentCallback(
                    WebshopPaymentCallbackRequest.builder()
                            .merchantOrderId(payment.getMerchantOrderId())
                            .pspPaymentId(payment.getId())
                            .status(payment.getStatus().name())
                            .paymentReference(payment.getGlobalTransactionId())
                            .paidAt(payment.getStatus() == PaymentStatus.SUCCESS ? Instant.now() : null)
                            .paymentMethod(method)
                            .build()
            );
        }
    }

    @Override
    @Transactional
    public HttpHeaders finalize(Long bankPaymentId) {
        Payment payment = paymentRepository.findByBankPaymentId(bankPaymentId)
                .orElseThrow(() -> new NotFoundException(String.format(BANK_NOT_FOUND_MSG, bankPaymentId)));

        waitForFinalStatus(payment.getId());

        Payment refreshed = getById(payment.getId());

        if (!isFinal(refreshed.getStatus())) {
            refreshed.setStatus(PaymentStatus.ERROR);
            paymentRepository.save(refreshed);
        }

        String targetUrl = resolveTargetUrl(refreshed);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", targetUrl);
        return headers;
    }

    private StartPaymentResponse startBankPayment(Long paymentId, String method) {
        Payment payment = getById(paymentId);

        if (payment.getStatus() == PaymentStatus.IN_PROGRESS
                && payment.getBankRedirectUrl() != null
                && !payment.getBankRedirectUrl().isBlank()) {
            return StartPaymentResponse.builder()
                    .redirectUrl(payment.getBankRedirectUrl())
                    .build();
        }

        if (isFinal(payment.getStatus())) {
            throw new BadRequestException("Payment is already finished: " + payment.getStatus());
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

        InitBankPaymentResponse bankInit;
        try {
            if ("QR".equalsIgnoreCase(method)) {
                bankInit = bankClient.initBankQrPayment(bankReq);
            } else {
                bankInit = bankClient.initBankPayment(bankReq);
            }
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.ERROR);
            paymentRepository.save(payment);
            throw e;
        }

        if (bankInit == null || bankInit.getRedirectUrl() == null || bankInit.getRedirectUrl().isBlank()) {
            payment.setStatus(PaymentStatus.ERROR);
            paymentRepository.save(payment);
            throw new BadRequestException("Bank initialization did not return a valid redirect URL.");
        }

        payment.setBankPaymentId(bankInit.getBankPaymentId());
        payment.setBankRedirectUrl(bankInit.getRedirectUrl());
        paymentRepository.save(payment);

        return StartPaymentResponse.builder()
                .redirectUrl(bankInit.getRedirectUrl())
                .build();
    }

    private Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_MSG, id)));
    }

    private void waitForFinalStatus(Long paymentId) {
        Instant start = Instant.now();
        Duration maxWait = Duration.ofMillis(1200);
        Duration sleep = Duration.ofMillis(150);

        while (Duration.between(start, Instant.now()).compareTo(maxWait) < 0) {
            Payment p = paymentRepository.findById(paymentId).orElse(null);
            if (p == null) return;
            if (isFinal(p.getStatus())) return;

            try {
                Thread.sleep(sleep.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean isFinal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.ERROR
                || status == PaymentStatus.CANCELED;
    }

    private PaymentStatus mapBankStatus(String bankStatus) {
        if (bankStatus == null) return PaymentStatus.ERROR;

        if ("SUCCESS".equalsIgnoreCase(bankStatus)) return PaymentStatus.SUCCESS;
        if ("FAILED".equalsIgnoreCase(bankStatus)) return PaymentStatus.FAILED;
        if ("ERROR".equalsIgnoreCase(bankStatus)) return PaymentStatus.ERROR;
        if ("IN_PROGRESS".equalsIgnoreCase(bankStatus) || "CREATED".equalsIgnoreCase(bankStatus))
            return PaymentStatus.IN_PROGRESS;

        return PaymentStatus.ERROR;
    }

    private String resolveTargetUrl(Payment payment) {
        PaymentStatus status = payment.getStatus();

        if (status == PaymentStatus.SUCCESS) return payment.getSuccessUrl();
        if (status == PaymentStatus.FAILED) return payment.getFailedUrl();
        return payment.getErrorUrl();
    }

    private String generateStan() {
        return UUID.randomUUID().toString();
    }
}