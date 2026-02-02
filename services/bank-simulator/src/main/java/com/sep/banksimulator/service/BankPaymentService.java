package com.sep.banksimulator.service;

import com.sep.banksimulator.client.CardPaymentClient;
import com.sep.banksimulator.client.PspClient;
import com.sep.banksimulator.client.QrPaymentClient;
import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentRequest;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import com.sep.banksimulator.dto.card.CardPaymentStatus;
import com.sep.banksimulator.dto.qr.*;
import com.sep.banksimulator.entity.BankPayment;
import com.sep.banksimulator.entity.BankPaymentStatus;
import com.sep.banksimulator.repository.BankPaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankPaymentService {

    private static final String PSP_FINALIZE_URL = "https://localhost:8080/psp/api/payments/finalize/";

    private static final Duration CARD_PAYMENT_TIMEOUT = Duration.ofMinutes(1);

    private static final String QR_CURRENCY = "RSD";
    private static final String RECEIVER_ACCOUNT = "840000003275384578";
    private static final String RECEIVER_NAME = "Vehicle Rental d.o.o.";
    private static final String PURPOSE = "Vehicle reservation";
    private static final String PAYMENT_CODE = "289";
    private static final String REFERENCE_NUMBER = null;

    private final BankPaymentRepository bankPaymentRepository;
    private final CardPaymentClient cardPaymentClient;
    private final QrPaymentClient qrServiceClient;
    private final PspClient pspClient;

    @Transactional
    public InitBankPaymentResponse init(InitBankPaymentRequest request) {
        BankPayment payment = BankPayment.builder()
                .pspPaymentId(request.getPspPaymentId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .stan(request.getStan())
                .pspTimestamp(request.getPspTimestamp())
                .status(BankPaymentStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        BankPayment saved = bankPaymentRepository.save(payment);

        return InitBankPaymentResponse.builder()
                .bankPaymentId(saved.getId())
                .redirectUrl("https://localhost:4400/checkout/" + saved.getId())
                .build();
    }

    @Transactional
    public InitBankPaymentResponse initQr(InitBankPaymentRequest request) {
        BankPayment payment = BankPayment.builder()
                .pspPaymentId(request.getPspPaymentId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .stan(request.getStan())
                .pspTimestamp(request.getPspTimestamp())
                .status(BankPaymentStatus.CREATED)
                .createdAt(Instant.now())
                .build();

        BankPayment saved = bankPaymentRepository.save(payment);

        return InitBankPaymentResponse.builder()
                .bankPaymentId(saved.getId())
                .redirectUrl("https://localhost:4400/qr-checkout/" + saved.getId())
                .build();
    }

    @Transactional
    public QrImageResponse getQr(Long bankPaymentId) {
        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        GenerateQrRequest req = GenerateQrRequest.builder()
                .bankPaymentId(payment.getId())
                .pspPaymentId(payment.getPspPaymentId())
                .amount(payment.getAmount())
                .currency(QR_CURRENCY)
                .receiverAccount(RECEIVER_ACCOUNT)
                .receiverName(RECEIVER_NAME)
                .purpose(PURPOSE)
                .paymentCode(PAYMENT_CODE)
                .referenceNumber(REFERENCE_NUMBER)
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp().toString())
                .build();

        GenerateQrResponse res = qrServiceClient.generate(req);

        if (res == null || res.getQrImageBase64() == null || res.getQrImageBase64().isBlank()) {
            throw new IllegalStateException("QR service did not return image");
        }

        return QrImageResponse.builder()
                .qrImageBase64(res.getQrImageBase64())
                .qrText(res.getQrText())
                .build();
    }

    @Transactional
    public String confirmQr(Long bankPaymentId, ConfirmQrPaymentRequest request) {
        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        if (payment.getStatus() == BankPaymentStatus.SUCCESS
                || payment.getStatus() == BankPaymentStatus.FAILED
                || payment.getStatus() == BankPaymentStatus.ERROR) {
            throw new IllegalStateException("Bank payment already finished: " + payment.getStatus());
        }

        payment.setStatus(BankPaymentStatus.IN_PROGRESS);
        bankPaymentRepository.save(payment);

        ValidateQrRequest validateReq = ValidateQrRequest.builder()
                .qrText(request.getQrText())
                .bankPaymentId(payment.getId())
                .pspPaymentId(payment.getPspPaymentId())
                .amount(payment.getAmount())
                .currency(QR_CURRENCY)
                .receiverAccount(RECEIVER_ACCOUNT)
                .receiverName(RECEIVER_NAME)
                .purpose(PURPOSE)
                .paymentCode(PAYMENT_CODE)
                .referenceNumber(REFERENCE_NUMBER)
                .stan(payment.getStan())
                .build();

        ValidateQrResponse validateRes = qrServiceClient.validate(validateReq);

        if (validateRes == null || !validateRes.isValid()) {
            payment.setStatus(BankPaymentStatus.FAILED);
            bankPaymentRepository.save(payment);

            sendCallbackToPsp(payment);

            return PSP_FINALIZE_URL + bankPaymentId;
        }

        payment.setStatus(BankPaymentStatus.SUCCESS);
        payment.setGlobalTransactionId(UUID.randomUUID().toString());
        payment.setAcquirerTimestamp(Instant.now());
        bankPaymentRepository.save(payment);

        sendCallbackToPsp(payment);

        return PSP_FINALIZE_URL + bankPaymentId;
    }

    @Transactional
    public String execute(Long bankPaymentId, ExecuteBankPaymentRequest request) {
        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        if (payment.getStatus() == BankPaymentStatus.SUCCESS
                || payment.getStatus() == BankPaymentStatus.FAILED
                || payment.getStatus() == BankPaymentStatus.ERROR) {
            throw new IllegalStateException("Bank payment already finished: " + payment.getStatus());
        }

        if (isExpired(payment)) {
            payment.setStatus(BankPaymentStatus.FAILED);
            bankPaymentRepository.save(payment);

            sendCallbackToPsp(payment);

            return PSP_FINALIZE_URL + bankPaymentId;
        }


        payment.setStatus(BankPaymentStatus.IN_PROGRESS);
        bankPaymentRepository.save(payment);

        AuthorizeCardPaymentResponse authRes;
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

            authRes = cardPaymentClient.authorize(authReq);
        } catch (Exception ex) {
            payment.setStatus(BankPaymentStatus.ERROR);
            bankPaymentRepository.save(payment);

            sendCallbackToPsp(payment);

            return PSP_FINALIZE_URL + bankPaymentId;
        }

        if (authRes == null || authRes.getStatus() == null) {
            payment.setStatus(BankPaymentStatus.ERROR);
            bankPaymentRepository.save(payment);

            sendCallbackToPsp(payment);

            return PSP_FINALIZE_URL + bankPaymentId;
        }

        if (authRes.getStatus() == CardPaymentStatus.SUCCESS) {
            payment.setStatus(BankPaymentStatus.SUCCESS);
        } else {
            payment.setStatus(BankPaymentStatus.FAILED);
        }

        payment.setGlobalTransactionId(authRes.getGlobalTransactionId());
        payment.setAcquirerTimestamp(authRes.getAcquirerTimestamp());

        bankPaymentRepository.save(payment);

        sendCallbackToPsp(payment);

        return PSP_FINALIZE_URL + bankPaymentId;
    }

    private void sendCallbackToPsp(BankPayment payment) {
        BankCallbackRequest callback = BankCallbackRequest.builder()
                .pspPaymentId(payment.getPspPaymentId())
                .bankPaymentId(payment.getId())
                .status(payment.getStatus())
                .globalTransactionId(payment.getGlobalTransactionId())
                .acquirerTimestamp(payment.getAcquirerTimestamp())
                .stan(payment.getStan())
                .pspTimestamp(payment.getPspTimestamp())
                .build();

        try {
            pspClient.sendBankCallback(callback);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isExpired(BankPayment p) {
        if (p.getCreatedAt() == null) return false;
        return Duration.between(p.getCreatedAt(), Instant.now()).compareTo(CARD_PAYMENT_TIMEOUT) > 0;
    }

}
