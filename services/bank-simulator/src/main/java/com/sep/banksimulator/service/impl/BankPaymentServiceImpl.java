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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankPaymentServiceImpl implements BankPaymentService {

    private static final String PSP_FINALIZE_URL = "https://localhost:8080/psp/api/payments/finalize/";
    private static final Duration CARD_PAYMENT_TIMEOUT = Duration.ofMinutes(1);
    private static final String QR_CURRENCY = "RSD";

    private final BankPaymentRepository bankPaymentRepository;
    private final CardPaymentClient cardPaymentClient;
    private final QrPaymentClient qrServiceClient;
    private final PspClient pspClient;

    @Override
    @Transactional
    public GenericPaymentResponse initialize(GenericPaymentRequest request) {
        BankPayment payment = BankPayment.builder()
                .pspPaymentId(request.getPspPaymentId())
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

        String selectedMethod = request.getMetadata().get("selectedMethod");
        String redirectUrl = "https://localhost:4400/checkout/" + saved.getId();

        if ("QR".equalsIgnoreCase(selectedMethod)) {
            redirectUrl = "https://localhost:4400/qr-checkout/" + saved.getId();
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
                .currency(QR_CURRENCY)
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp().toString())
                .receiverName(payment.getReceiverName())
                .receiverAccount(payment.getReceiverAccount())
                .purpose("Payment for Reservation #" + payment.getPspPaymentId())
                .paymentCode("289")
                .build();

        GenerateQrResponse res = qrServiceClient.generate(req);

        if (res == null || res.getQrImageBase64() == null) {
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
        bankPaymentRepository.save(payment);

        ValidateQrRequest validateReq = ValidateQrRequest.builder()
                .qrText(request.getQrText())
                .bankPaymentId(payment.getId())
                .pspPaymentId(payment.getPspPaymentId())
                .amount(payment.getAmount())
                .currency(QR_CURRENCY)
                .stan(payment.getStan())
                .receiverName(payment.getReceiverName())
                .receiverAccount(payment.getReceiverAccount())
                .build();

        ValidateQrResponse validateRes = qrServiceClient.validate(validateReq);

        if (validateRes == null || !validateRes.isValid()) {
            finalizePayment(payment, BankPaymentStatus.FAILED, null, null);
        } else {
            finalizePayment(payment, BankPaymentStatus.SUCCESS, UUID.randomUUID().toString(), Instant.now());
        }

        return PSP_FINALIZE_URL + payment.getPspPaymentId();
    }

    @Override
    @Transactional
    public String execute(Long bankPaymentId, ExecuteBankPaymentRequest request) {
        BankPayment payment = getById(bankPaymentId);
        checkFinalStatus(payment);

        if (isExpired(payment)) {
            finalizePayment(payment, BankPaymentStatus.FAILED, null, null);
            return PSP_FINALIZE_URL + payment.getPspPaymentId();
        }

        payment.setStatus(BankPaymentStatus.IN_PROGRESS);
        bankPaymentRepository.save(payment);

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
                finalizePayment(payment, BankPaymentStatus.SUCCESS, authRes.getGlobalTransactionId(), authRes.getAcquirerTimestamp());
            } else {
                finalizePayment(payment, BankPaymentStatus.FAILED, null, null);
            }
        } catch (Exception ex) {
            finalizePayment(payment, BankPaymentStatus.ERROR, null, null);
        }

        return PSP_FINALIZE_URL + payment.getPspPaymentId();
    }

    private void finalizePayment(BankPayment payment, BankPaymentStatus status, String gid, Instant timestamp) {
        payment.setStatus(status);
        payment.setGlobalTransactionId(gid);
        payment.setAcquirerTimestamp(timestamp);
        bankPaymentRepository.save(payment);

        GenericCallbackRequest callback = GenericCallbackRequest.builder()
                .pspPaymentId(payment.getPspPaymentId())
                .externalPaymentId(payment.getId().toString())
                .status(status.name())
                .globalTransactionId(gid)
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp())
                .acquirerTimestamp(timestamp)
                .build();

        try {
            pspClient.sendCallback(callback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private BankPayment getById(Long id) {
        return bankPaymentRepository.findById(id)
                .orElseThrow(() -> new NotAcceptableException("Payment with id: " + id + "not found."));
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