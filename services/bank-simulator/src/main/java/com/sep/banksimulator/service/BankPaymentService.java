package com.sep.banksimulator.service;

import com.sep.banksimulator.client.CardPaymentClient;
import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.entity.BankPayment;
import com.sep.banksimulator.entity.BankPaymentStatus;
import com.sep.banksimulator.repository.BankPaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BankPaymentService {

    private static final String PSP_CALLBACK_URL = "http://localhost:8080/psp/api/bank/callback";
    private static final String PSP_FINALIZE_URL = "http://localhost:8080/psp/api/payments/finalize/";

    private final BankPaymentRepository bankPaymentRepository;
    private final RestTemplate restTemplate;
    private final CardPaymentClient cardPaymentClient;

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
    public String execute(Long bankPaymentId, ExecuteBankPaymentRequest request) {
        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        if (payment.getStatus() == BankPaymentStatus.SUCCESS
                || payment.getStatus() == BankPaymentStatus.FAILED
                || payment.getStatus() == BankPaymentStatus.ERROR) {
            throw new IllegalStateException("Bank payment already finished: " + payment.getStatus());
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

            sendCallbackToPsp(payment, null);

            return PSP_FINALIZE_URL + bankPaymentId;
        }

        if (authRes == null || authRes.getStatus() == null) {
            payment.setStatus(BankPaymentStatus.ERROR);
            bankPaymentRepository.save(payment);

            sendCallbackToPsp(payment, null);

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

        sendCallbackToPsp(payment, authRes);

        return PSP_FINALIZE_URL + bankPaymentId;
    }

    private void sendCallbackToPsp(BankPayment payment, AuthorizeCardPaymentResponse authRes) {
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
        } catch (RestClientException ignored) {
        }
    }
}
