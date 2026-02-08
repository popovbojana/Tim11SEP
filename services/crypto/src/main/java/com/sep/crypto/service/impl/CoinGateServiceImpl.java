package com.sep.crypto.service.impl;

import com.sep.crypto.dto.*;
import com.sep.crypto.entity.CryptoTransaction;
import com.sep.crypto.repository.CryptoTransactionRepository;
import com.sep.crypto.service.CoinGateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class CoinGateServiceImpl implements CoinGateService {

    private final RestTemplate externalRestTemplate;
    private final RestTemplate loadBalancedRestTemplate;
    private final CryptoTransactionRepository cryptoTransactionRepository;

    @Value("${coingate.api.url}")
    private String apiUrl;

    @Value("${coingate.api.token}")
    private String apiToken;

    @Value("${crypto.app.callback-url}")
    private String appCallbackUrl;

    public CoinGateServiceImpl(@Qualifier("externalRestTemplate") RestTemplate externalRestTemplate,
                               @Qualifier("restTemplate") RestTemplate loadBalancedRestTemplate,
                               CryptoTransactionRepository cryptoTransactionRepository) {
        this.externalRestTemplate = externalRestTemplate;
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
        this.cryptoTransactionRepository = cryptoTransactionRepository;
    }

    @Override
    public GenericPaymentResponse createOrder(GenericPaymentRequest request) {
        log.info("📨 Creating CoinGate order for PSP payment ID: {}", request.getPspPaymentId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiToken);

        String currency = request.getCurrency().equals("€") ? "EUR" : request.getCurrency();
        log.info("💱 Currency resolved: {} → {}", request.getCurrency(), currency);

        CoinGateOrderRequest cgRequest = CoinGateOrderRequest.builder()
                .orderId(request.getPspPaymentId().toString())
                .priceAmount(request.getAmount())
                .priceCurrency(currency)
                .receiveCurrency("BTC")
                .title("Reservation #" + request.getPspPaymentId())
                .callbackUrl(appCallbackUrl)
                .successUrl("https://localhost:8080/psp/api/payments/finalize/" + request.getPspPaymentId())
                .cancelUrl("https://localhost:4200/payment/cancel")
                .build();

        log.info("⚙️ CoinGate request built — amount: {} {}, receive: BTC", request.getAmount(), currency);

        HttpEntity<CoinGateOrderRequest> entity = new HttpEntity<>(cgRequest, headers);

        try {
            ResponseEntity<CoinGateOrderResponse> response = externalRestTemplate.exchange(
                    apiUrl + "/orders",
                    HttpMethod.POST,
                    entity,
                    CoinGateOrderResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("✅ CoinGate order created — ID: {}, payment URL received", response.getBody().getId());

                Map<String, String> metadata = request.getMetadata();

                CryptoTransaction transaction = CryptoTransaction.builder()
                        .pspPaymentId(request.getPspPaymentId())
                        .coingateOrderId(response.getBody().getId().toString())
                        .status("CREATED")
                        .merchantOrderId(metadata != null ? metadata.get("merchantOrderId") : null)
                        .merchantKey(metadata != null ? metadata.get("merchantKey") : null)
                        .currency(currency)
                        .amount(request.getAmount())
                        .createdAt(Instant.now())
                        .build();

                cryptoTransactionRepository.save(transaction);
                log.info("💾 Transaction saved — CoinGate order ID: {}, status: CREATED", transaction.getCoingateOrderId());

                return GenericPaymentResponse.builder()
                        .redirectUrl(response.getBody().getPaymentUrl())
                        .externalPaymentId(response.getBody().getId().toString())
                        .build();
            }
        } catch (Exception e) {
            log.error("❌ CoinGate API call failed: {}", e.getMessage(), e);
            throw e;
        }

        log.error("❌ CoinGate order creation failed — no successful response");
        throw new RuntimeException("CoinGate order creation failed");
    }

    @Override
    public void processCallback(CoinGateCallback callback) {
        log.info("📨 Received CoinGate callback — order ID: {}, status: {}", callback.getId(), callback.getStatus());

        CryptoTransaction transaction = cryptoTransactionRepository.findByCoingateOrderId(callback.getId().toString())
                .orElseThrow(() -> {
                    log.error("❌ Transaction NOT FOUND for CoinGate order ID: {}", callback.getId());
                    return new RuntimeException("Transaction not found");
                });

        log.info("🔍 Transaction found — PSP payment ID: {}, current status: {}", transaction.getPspPaymentId(), transaction.getStatus());

        if (!"paid".equalsIgnoreCase(callback.getStatus())) {
            log.warn("⚠️ Callback status is '{}', skipping — only 'paid' is processed", callback.getStatus());
            return;
        }

        transaction.setStatus("PAID");
        cryptoTransactionRepository.save(transaction);
        log.info("✅ Transaction updated to PAID — CoinGate order ID: {}", transaction.getCoingateOrderId());

        GenericCallbackRequest callbackRequest = GenericCallbackRequest.builder()
                .pspPaymentId(transaction.getPspPaymentId())
                .merchantOrderId(transaction.getMerchantOrderId())
                .status("SUCCESS")
                .externalPaymentId(transaction.getCoingateOrderId())
                .globalTransactionId(transaction.getCoingateOrderId())
                .stan(transaction.getCoingateOrderId())
                .paymentMethod("CRYPTO")
                .acquirerTimestamp(Instant.now())
                .pspTimestamp(transaction.getCreatedAt())
                .build();

        try {
            log.info("📨 Sending callback to PSP for payment ID: {}", transaction.getPspPaymentId());
            loadBalancedRestTemplate.postForObject("https://PSP/api/payments/callback", callbackRequest, Void.class);
            log.info("✅ PSP callback sent successfully");
        } catch (Exception e) {
            log.error("❌ Failed to send callback to PSP: {}", e.getMessage(), e);
        }
    }
}