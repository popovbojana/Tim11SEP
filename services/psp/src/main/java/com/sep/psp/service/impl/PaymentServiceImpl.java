package com.sep.psp.service.impl;

import com.sep.psp.dto.payment.*;
import com.sep.psp.entity.*;
import com.sep.psp.exception.BadRequestException;
import com.sep.psp.exception.NotFoundException;
import com.sep.psp.repository.MerchantRepository;
import com.sep.psp.repository.PaymentMethodRepository;
import com.sep.psp.repository.PaymentRepository;
import com.sep.psp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final MerchantRepository merchantRepository;
    private final RestTemplate restTemplate;

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
                .stan(UUID.randomUUID().toString())
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
    public StartPaymentResponse startPayment(Long paymentId, String methodName) {
        Payment payment = getById(paymentId);

        if (payment.getStatus() == PaymentStatus.IN_PROGRESS && payment.getExternalRedirectUrl() != null) {
            return StartPaymentResponse.builder().redirectUrl(payment.getExternalRedirectUrl()).build();
        }

        if (isFinal(payment.getStatus())) {
            throw new BadRequestException("Payment is already finished.");
        }

        Merchant merchant = merchantRepository.findByMerchantKey(payment.getMerchantKey())
                .orElseThrow(() -> new NotFoundException("Merchant not found"));

        PaymentMethod method = paymentMethodRepository.findByName(methodName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Method not supported: " + methodName));

        payment.setStatus(PaymentStatus.IN_PROGRESS);
        payment.setPaymentMethod(methodName.toUpperCase());
        paymentRepository.save(payment);

        GenericPaymentRequest genericReq = GenericPaymentRequest.builder()
                .pspPaymentId(payment.getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .stan(payment.getStan())
                .pspTimestamp(Instant.now())
                .metadata(Map.of(
                        "selectedMethod", methodName,
                        "receiverName", merchant.getFullName(),
                        "receiverAccount", merchant.getBankAccount(),
                        "merchantOrderId", payment.getMerchantOrderId(),
                        "merchantKey", payment.getMerchantKey()
                ))
                .build();

        String url = "https://" + method.getServiceName() + "/api/payments/init";
        try {
            GenericPaymentResponse response = restTemplate.postForObject(url, genericReq, GenericPaymentResponse.class);

            if (response == null || response.getRedirectUrl() == null) {
                throw new BadRequestException("Provider failed to initialize.");
            }

            payment.setExternalPaymentId(response.getExternalPaymentId());
            payment.setExternalRedirectUrl(response.getRedirectUrl());
            paymentRepository.save(payment);

            return StartPaymentResponse.builder().redirectUrl(response.getRedirectUrl()).build();
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.ERROR);
            paymentRepository.save(payment);
            e.printStackTrace();
            throw new BadRequestException("Communication error with provider.");
        }
    }

    @Override
    @Transactional
    public void handleCallback(GenericCallbackRequest request) {
        Payment payment = getById(request.getPspPaymentId());
        if (isFinal(payment.getStatus())) return;

        payment.setExternalPaymentId(request.getExternalPaymentId());
        payment.setStatus(mapStatus(request.getStatus()));
        payment.setGlobalTransactionId(request.getGlobalTransactionId());
        payment.setAcquirerTimestamp(request.getAcquirerTimestamp());
        payment.setStan(request.getStan());

        paymentRepository.save(payment);

        request.setMerchantOrderId(payment.getMerchantOrderId());
        request.setPaymentMethod(payment.getPaymentMethod());

        Merchant merchant = merchantRepository.findByMerchantKey(payment.getMerchantKey())
                .orElse(null);

        if (merchant != null && merchant.getServiceName() != null && !merchant.getServiceName().isBlank()) {
            try {
                restTemplate.postForObject("https://" + merchant.getServiceName() + "/api/payments/callback", request, Void.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment payment = getById(id);
        return PaymentResponse.builder()
                .id(payment.getId()).amount(payment.getAmount()).currency(payment.getCurrency())
                .status(payment.getStatus()).merchantKey(payment.getMerchantKey())
                .merchantOrderId(payment.getMerchantOrderId()).successUrl(payment.getSuccessUrl())
                .failedUrl(payment.getFailedUrl()).errorUrl(payment.getErrorUrl()).build();
    }

    @Override
    @Transactional
    public HttpHeaders finalize(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment with id: " + id + " not found."));

        waitForFinalStatus(payment.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", resolveTargetUrl(getById(payment.getId())));
        return headers;
    }

    private Payment getById(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new NotFoundException("Payment not found."));
    }

    private void waitForFinalStatus(Long paymentId) {
        Instant start = Instant.now();
        while (Duration.between(start, Instant.now()).toMillis() < 1200) {
            Payment p = paymentRepository.findById(paymentId).orElse(null);
            if (p != null && isFinal(p.getStatus())) return;
            try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    private boolean isFinal(PaymentStatus status) {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILED || status == PaymentStatus.ERROR || status == PaymentStatus.CANCELED;
    }

    private PaymentStatus mapStatus(String status) {
        try { return PaymentStatus.valueOf(status.toUpperCase()); }
        catch (Exception e) { return PaymentStatus.ERROR; }
    }

    private String resolveTargetUrl(Payment payment) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) return payment.getSuccessUrl();
        if (payment.getStatus() == PaymentStatus.FAILED) return payment.getFailedUrl();
        return payment.getErrorUrl();
    }
}