package com.sep.banksimulator.service.impl;

import com.sep.banksimulator.client.CardPaymentClient;
import com.sep.banksimulator.client.PspClient;
import com.sep.banksimulator.client.QrPaymentClient;
import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.card.*;
import com.sep.banksimulator.dto.qr.*;
import com.sep.banksimulator.entity.BankPayment;
import com.sep.banksimulator.entity.BankPaymentStatus;
import com.sep.banksimulator.exception.BadRequestException;
import com.sep.banksimulator.repository.BankPaymentRepository;
import com.sep.banksimulator.service.BankPaymentService;
import jakarta.ws.rs.NotAcceptableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankPaymentServiceImpl implements BankPaymentService {

    private static final String PSP_FINALIZE_URL = "https://localhost:8080/psp/api/payments/finalize/";
    private static final Duration CARD_PAYMENT_TIMEOUT = Duration.ofMinutes(15);

    @Value("${bank.frontend.url}")
    private String bankFrontUrl;

    private final BankPaymentRepository bankPaymentRepository;
    private final CardPaymentClient cardPaymentClient;
    private final QrPaymentClient qrServiceClient;
    private final PspClient pspClient;

    @Override
    @Transactional
    public GenericPaymentResponse initialize(GenericPaymentRequest request) {
        log.info("Initializing bank payment for PSP Payment ID: {}", request.getPspPaymentId());

        String merchantOrderId = request.getMetadata().get("merchantOrderId");
        String selectedMethod = request.getMetadata().get("selectedMethod");

        BankPayment payment = BankPayment.builder()
                .pspPaymentId(request.getPspPaymentId())
                .merchantOrderId(merchantOrderId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .stan(request.getStan())
                .pspTimestamp(request.getPspTimestamp())
                .status(BankPaymentStatus.CREATED)
                .createdAt(Instant.now())
                .receiverName(request.getMetadata().get("receiverName"))
                .receiverAccount(request.getMetadata().get("receiverAccount"))
                .build();

        BankPayment saved = bankPaymentRepository.save(payment);

        String redirectUrl = bankFrontUrl + "/checkout/" + saved.getId();
        if ("QR".equalsIgnoreCase(selectedMethod)) {
            redirectUrl = bankFrontUrl + "/qr-checkout/" + saved.getId();
        }

        return GenericPaymentResponse.builder()
                .externalPaymentId(saved.getId().toString())
                .redirectUrl(redirectUrl)
                .build();
    }

    @Override
    @Transactional
    public QrImageResponse getQr(Long bankPaymentId) {
        BankPayment payment = getById(bankPaymentId);

        GenerateQrRequest req = GenerateQrRequest.builder()
                .bankPaymentId(payment.getId())
                .pspPaymentId(payment.getPspPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp().toString())
                .receiverName(payment.getReceiverName())
                .receiverAccount(payment.getReceiverAccount())
                .purpose("Payment for " + payment.getReceiverName())
                .paymentCode("289")
                .build();

        log.info("Requesting QR generation for Payment ID: {}", bankPaymentId);
        GenerateQrResponse res = qrServiceClient.generate(req);

        if (res == null || res.getQrImageBase64() == null) {
            log.error("QR Service returned empty response for Payment ID: {}", bankPaymentId);
            throw new BadRequestException("QR service failed.");
        }

        return QrImageResponse.builder()
                .qrImageBase64(res.getQrImageBase64())
                .qrText(res.getQrText())
                .build();
    }

    @Override
    @Transactional
    public String confirmQr(Long bankPaymentId, ConfirmQrPaymentRequest request) {
        BankPayment payment = getById(bankPaymentId);
        checkFinalStatus(payment);

        payment.setStatus(BankPaymentStatus.IN_PROGRESS);
        bankPaymentRepository.saveAndFlush(payment);

        ValidateQrRequest validateReq = ValidateQrRequest.builder()
                .qrText(request.getQrText())
                .bankPaymentId(payment.getId())
                .pspPaymentId(payment.getPspPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .stan(payment.getStan())
                .receiverName(payment.getReceiverName())
                .receiverAccount(payment.getReceiverAccount())
                .createdAt(payment.getCreatedAt())
                .build();

        try {
            ValidateQrResponse validateRes = qrServiceClient.validate(validateReq);
            if (validateRes != null && validateRes.isValid()) {
                finalizePayment(payment, BankPaymentStatus.SUCCESS, UUID.randomUUID().toString(), Instant.now(), "QR");
            } else {
                finalizePayment(payment, BankPaymentStatus.FAILED, null, null, "QR");
            }
        } catch (Exception e) {
            log.error("QR Validation error: {}", e.getMessage());
            finalizePayment(payment, BankPaymentStatus.ERROR, null, null, "QR");
        }

        return PSP_FINALIZE_URL + payment.getPspPaymentId();
    }

    @Override
    @Transactional
    public String execute(Long bankPaymentId, ExecuteBankPaymentRequest request) {
        BankPayment payment = getById(bankPaymentId);
        checkFinalStatus(payment);

        if (isExpired(payment)) {
            log.warn("Payment session expired for ID: {}", bankPaymentId);
            finalizePayment(payment, BankPaymentStatus.FAILED, null, null, "CARD");
            return PSP_FINALIZE_URL + payment.getPspPaymentId();
        }

        payment.setStatus(BankPaymentStatus.IN_PROGRESS);
        bankPaymentRepository.saveAndFlush(payment);

        try {
            AuthorizeCardPaymentRequest authReq = AuthorizeCardPaymentRequest.builder()
                    .bankPaymentId(payment.getId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .pan(request.getPan())
                    .securityCode(request.getSecurityCode())
                    .cardHolderName(request.getCardHolderName())
                    .expiry(request.getExpiry())
                    .build();

            AuthorizeCardPaymentResponse authRes = cardPaymentClient.authorize(authReq);

            if (authRes != null && authRes.getStatus() == CardPaymentStatus.SUCCESS) {
                finalizePayment(payment, BankPaymentStatus.SUCCESS, authRes.getGlobalTransactionId(), authRes.getAcquirerTimestamp(), "CARD");
            } else {
                finalizePayment(payment, BankPaymentStatus.FAILED, null, null, "CARD");
            }
        } catch (Exception ex) {
            log.error("Critical error during card execution for ID {}: {}", bankPaymentId, ex.getMessage());
            finalizePayment(payment, BankPaymentStatus.ERROR, null, null, "CARD");
        }

        return PSP_FINALIZE_URL + payment.getPspPaymentId();
    }

    private void finalizePayment(BankPayment payment, BankPaymentStatus status, String gid, Instant timestamp, String method) {
        payment.setStatus(status);
        payment.setGlobalTransactionId(gid);
        payment.setAcquirerTimestamp(timestamp);
        bankPaymentRepository.saveAndFlush(payment);

        log.info("Payment {} finalized with status {}", payment.getId(), status);

        GenericCallbackRequest callback = GenericCallbackRequest.builder()
                .pspPaymentId(payment.getPspPaymentId())
                .merchantOrderId(payment.getMerchantOrderId())
                .amount(payment.getAmount())
                .paymentMethod(method)
                .externalPaymentId(payment.getId().toString())
                .status(status.name())
                .globalTransactionId(gid)
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp())
                .acquirerTimestamp(timestamp)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    pspClient.sendCallback(callback);
                    log.info("Callback sent to PSP for Payment ID: {}", payment.getId());
                } catch (Exception e) {
                    log.error("Failed to send callback to PSP: {}. Reconciliation required.", e.getMessage());
                }
            }
        });
    }

    private BankPayment getById(Long id) {
        return bankPaymentRepository.findById(id)
                .orElseThrow(() -> new NotAcceptableException("Payment with id: " + id + " not found."));
    }

    private void checkFinalStatus(BankPayment p) {
        if (p.getStatus() == BankPaymentStatus.SUCCESS || p.getStatus() == BankPaymentStatus.FAILED) {
            throw new BadRequestException("Payment already finished.");
        }
    }

    private boolean isExpired(BankPayment p) {
        return p.getCreatedAt() != null && Duration.between(p.getCreatedAt(), Instant.now()).compareTo(CARD_PAYMENT_TIMEOUT) > 0;
    }
}