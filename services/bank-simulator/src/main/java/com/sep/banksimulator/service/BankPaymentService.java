package com.sep.banksimulator.service;

import com.sep.banksimulator.client.CardPaymentClient;
import com.sep.banksimulator.client.QrPaymentClient;
import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentRequest;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import com.sep.banksimulator.dto.card.CardPaymentStatus;
import com.sep.banksimulator.dto.qr.*;
import com.sep.banksimulator.entity.*;
import com.sep.banksimulator.repository.BankPaymentRepository;
import com.sep.banksimulator.repository.BankAccountRepository;
import com.sep.banksimulator.repository.BankCardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankPaymentService {

    private static final String PSP_CALLBACK_URL = "http://localhost:8080/psp/api/bank/callback";
    private static final String PSP_FINALIZE_URL = "http://localhost:8080/psp/api/payments/finalize/";
    private static final Duration CARD_PAYMENT_TIMEOUT = Duration.ofMinutes(5);
    private static final String QR_CURRENCY = "RSD";
    private static final String RECEIVER_ACCOUNT = "840000003275384578";
    private static final String RECEIVER_NAME = "Vehicle Rental d.o.o.";
    private static final String PURPOSE = "Vehicle reservation";
    private static final String PAYMENT_CODE = "289";
    private static final String REFERENCE_NUMBER = null;

    private final BankPaymentRepository bankPaymentRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BankCardRepository bankCardRepository;
    private final RestTemplate restTemplate;
    private final CardPaymentClient cardPaymentClient;
    private final QrPaymentClient qrServiceClient;

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
                .redirectUrl("http://localhost:4400/checkout/" + saved.getId())
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
                .redirectUrl("http://localhost:4400/qr-checkout/" + saved.getId())
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

    public AuthorizeCardPaymentResponse execute(Long bankPaymentId, ExecuteBankPaymentRequest request) {
        log.info("💳 Starting card payment execution for bankPaymentId: {}", bankPaymentId);

        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        if (isExpired(payment)) {
            log.warn("⏰ Payment expired for bankPaymentId: {}", bankPaymentId);
            return markAsFailed(payment, FailureReason.EXPIRED_CARD);
        }

        try {
            // Pozivamo CardAuthorizationService koji radi SVE provere (PAN, CVV, expiry, balance)
            AuthorizeCardPaymentRequest authReq = AuthorizeCardPaymentRequest.builder()
                    .bankPaymentId(payment.getId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .pan(request.getPan())
                    .securityCode(request.getSecurityCode())
                    .cardHolderName(request.getCardHolderName())
                    .expiry(request.getExpiry())
                    .build();

            log.info("🏦 Calling CardAuthorizationService for authorization");
            AuthorizeCardPaymentResponse authRes = cardPaymentClient.authorize(authReq);

            if (authRes != null && authRes.getStatus() == CardPaymentStatus.SUCCESS) {
                log.info("✅ Authorization successful! Finalizing payment...");

                // Samo sada zaključavamo sredstva
                String hashedPan = hashPan(request.getPan());
                BankCard card = bankCardRepository.findByPan(hashedPan).orElseThrow();

                finalizeSuccessfulPayment(payment.getId(), card.getAccount().getId());

                return authRes;
            } else {
                log.warn("❌ Authorization failed. Reason: {}", authRes != null ? authRes.getReason() : "Unknown");
                FailureReason reason = authRes != null && authRes.getReason() != null
                        ? authRes.getReason()
                        : FailureReason.INVALID_CARD_DATA;
                return markAsFailed(payment, reason);
            }

        } catch (Exception ex) {
            log.error("💥 Exception during card authorization: {}", ex.getMessage(), ex);
            return markAsFailed(payment, FailureReason.SYSTEM_ERROR);
        }
    }

    @Transactional
    public void finalizeSuccessfulPayment(Long paymentId, Long accountId) {
        log.info("💰 Finalizing payment - deducting funds from account");

        BankPayment payment = bankPaymentRepository.findById(paymentId).orElseThrow();
        BankAccount account = bankAccountRepository.findById(accountId).orElseThrow();

        account.setBalance(account.getBalance() - payment.getAmount());
        bankAccountRepository.save(account);

        payment.setStatus(BankPaymentStatus.SUCCESS);
        payment.setGlobalTransactionId(UUID.randomUUID().toString());
        payment.setAcquirerTimestamp(Instant.now());
        bankPaymentRepository.save(payment);

        log.info("✅ Payment finalized successfully! New balance: {}", account.getBalance());

        // sendCallbackToPsp(payment); // Zakomentarisano po instrukciji
    }

    private AuthorizeCardPaymentResponse markAsFailed(BankPayment payment, FailureReason reason) {
        log.warn("❌ Marking payment as FAILED. Reason: {}", reason);

        payment.setStatus(BankPaymentStatus.FAILED);
        bankPaymentRepository.save(payment);

        // sendCallbackToPsp(payment); // Zakomentarisano po instrukciji

        return AuthorizeCardPaymentResponse.builder()
                .status(CardPaymentStatus.FAILED)
                .reason(reason)
                .build();
    }

    private String hashPan(String pan) {
        try {
            String cleanPan = pan.replaceAll("\\D", "");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(cleanPan.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
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
            restTemplate.postForObject(PSP_CALLBACK_URL, callback, Void.class);
        } catch (RestClientException ignored) {}
    }

    private boolean isExpired(BankPayment p) {
        if (p.getCreatedAt() == null) return false;
        return Duration.between(p.getCreatedAt(), Instant.now()).compareTo(CARD_PAYMENT_TIMEOUT) > 0;
    }
}