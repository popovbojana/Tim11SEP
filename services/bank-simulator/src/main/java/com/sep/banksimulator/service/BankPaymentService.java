package com.sep.banksimulator.service;

import com.sep.banksimulator.dto.InitBankPaymentRequest;
import com.sep.banksimulator.dto.InitBankPaymentResponse;
import com.sep.banksimulator.entity.BankPayment;
import com.sep.banksimulator.entity.BankPaymentStatus;
import com.sep.banksimulator.repository.BankPaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankPaymentService {

    private final BankPaymentRepository bankPaymentRepository;
    private final RestTemplate restTemplate;

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
    public String execute(Long bankPaymentId, boolean success) {
        BankPayment payment = bankPaymentRepository.findById(bankPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Bank payment not found"));

        payment.setStatus(success ? BankPaymentStatus.SUCCESS : BankPaymentStatus.FAILED);
        payment.setGlobalTransactionId(UUID.randomUUID().toString());
        payment.setAcquirerTimestamp(Instant.now());

        bankPaymentRepository.save(payment);

        String callbackUrl = "http://localhost:8080/psp/api/bank/callback";

        restTemplate.postForObject(
                callbackUrl,
                payment,
                Void.class
        );

        return "http://localhost:8080/psp/api/payments/finalize/" + bankPaymentId;
    }
}
