package com.sep.paypal.service.impl;

import com.sep.paypal.dto.*;
import com.sep.paypal.entity.PaypalTransaction;
import com.sep.paypal.repository.PaypalTransactionRepository;
import com.sep.paypal.service.PaypalAuthService;
import com.sep.paypal.service.PaypalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PaypalServiceImpl implements PaypalService {

    private final PaypalAuthService paypalAuthService;
    private final RestTemplate externalRestTemplate;
    private final RestTemplate loadBalancedRestTemplate;
    private final PaypalTransactionRepository paypalTransactionRepository;

    private static final String PSP_FINALIZE_URL = "https://localhost:8080/psp/api/payments/finalize/";
    private static final String PSP_ERROR_URL = "https://localhost:8080/psp/api/payments/error";

    public PaypalServiceImpl(PaypalAuthService paypalAuthService,
                             @Qualifier("externalRestTemplate") RestTemplate externalRestTemplate,
                             @Qualifier("restTemplate") RestTemplate loadBalancedRestTemplate,
                             PaypalTransactionRepository paypalTransactionRepository) {
        this.paypalAuthService = paypalAuthService;
        this.externalRestTemplate = externalRestTemplate;
        this.loadBalancedRestTemplate = loadBalancedRestTemplate;
        this.paypalTransactionRepository = paypalTransactionRepository;
    }

    @Override
    public GenericPaymentResponse createOrder(GenericPaymentRequest genericRequest) {
        log.info("📨 Creating PayPal order for PSP payment ID: {}", genericRequest.getPspPaymentId());

        String token = paypalAuthService.getAccessToken();

        PaypalOrderRequest request = new PaypalOrderRequest();
        request.setIntent("CAPTURE");

        PaypalOrderRequest.Amount orderAmount = new PaypalOrderRequest.Amount();
        String currency = genericRequest.getCurrency();
        if (currency.equals("€")) currency = "EUR";
        else if (currency.equals("$")) currency = "USD";

        orderAmount.setCurrency_code(currency);
        orderAmount.setValue(String.format("%.2f", genericRequest.getAmount()).replace(",", "."));

        log.info("💱 Order amount: {} {}", orderAmount.getValue(), currency);

        PaypalOrderRequest.PurchaseUnit unit = new PaypalOrderRequest.PurchaseUnit();
        unit.setAmount(orderAmount);
        request.setPurchase_units(List.of(unit));

        PaypalOrderRequest.ApplicationContext context = new PaypalOrderRequest.ApplicationContext();
        context.setReturn_url("https://localhost:8086/api/payments/confirm");
        context.setCancel_url("https://localhost:4200/payment/cancel");
        request.setApplication_context(context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PaypalOrderRequest> entity = new HttpEntity<>(request, headers);

        try {
            PaypalOrderResponse response = externalRestTemplate.postForObject(
                    "https://api-m.sandbox.paypal.com/v2/checkout/orders",
                    entity,
                    PaypalOrderResponse.class
            );

            if (response != null) {
                log.info("✅ PayPal order created — order ID: {}", response.getId());

                Map<String, String> metadata = genericRequest.getMetadata();

                PaypalTransaction transaction = PaypalTransaction.builder()
                        .pspPaymentId(genericRequest.getPspPaymentId())
                        .paypalOrderId(response.getId())
                        .status("CREATED")
                        .merchantOrderId(metadata.get("merchantOrderId"))
                        .merchantKey(metadata.get("merchantKey"))
                        .currency(currency)
                        .createdAt(Instant.now())
                        .build();

                paypalTransactionRepository.save(transaction);
                log.info("💾 Transaction saved — PayPal order ID: {}, status: CREATED", response.getId());

                String approveUrl = response.getLinks().stream()
                        .filter(link -> link.getRel().equals("approve"))
                        .findFirst()
                        .map(PaypalOrderResponse.Link::getHref)
                        .orElse("");

                log.info("🔗 Approve URL: {}", approveUrl);

                return GenericPaymentResponse.builder()
                        .redirectUrl(approveUrl)
                        .externalPaymentId(response.getId())
                        .build();
            }
        } catch (Exception e) {
            log.error("❌ PayPal order creation failed: {}", e.getMessage(), e);
            throw e;
        }

        log.error("❌ PayPal order creation returned null response");
        throw new RuntimeException("PayPal order creation failed");
    }

    @Override
    public String captureOrder(String paypalOrderId) {
        log.info("⚙️ Capturing PayPal order: {}", paypalOrderId);

        try {
            String token = paypalAuthService.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<Map> response = externalRestTemplate.postForEntity(
                    "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + paypalOrderId + "/capture",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ PayPal capture successful for order: {}", paypalOrderId);

                PaypalTransaction transaction = paypalTransactionRepository.findByPaypalOrderId(paypalOrderId)
                        .orElseThrow(() -> {
                            log.error("❌ Transaction NOT FOUND for PayPal order ID: {}", paypalOrderId);
                            return new RuntimeException("Transaction not found");
                        });

                transaction.setStatus("COMPLETED");
                paypalTransactionRepository.save(transaction);
                log.info("💾 Transaction updated to COMPLETED — PayPal order ID: {}", paypalOrderId);

                notifyPsp(transaction);
                return PSP_FINALIZE_URL + transaction.getPaypalOrderId();
            }

            log.error("❌ PayPal capture failed — status: {}", response.getStatusCode());
            return PSP_ERROR_URL;
        } catch (Exception e) {
            log.error("❌ PayPal capture error for order {}: {}", paypalOrderId, e.getMessage(), e);
            return PSP_ERROR_URL;
        }
    }

    private void notifyPsp(PaypalTransaction transaction) {
        log.info("📨 Sending callback to PSP for payment ID: {}", transaction.getPspPaymentId());

        GenericCallbackRequest callback = GenericCallbackRequest.builder()
                .pspPaymentId(transaction.getPspPaymentId())
                .merchantOrderId(transaction.getMerchantOrderId())
                .status("SUCCESS")
                .externalPaymentId(transaction.getPaypalOrderId())
                .globalTransactionId(transaction.getPaypalOrderId())
                .stan(transaction.getPaypalOrderId())
                .paymentMethod("PAYPAL")
                .acquirerTimestamp(Instant.now())
                .pspTimestamp(transaction.getCreatedAt())
                .build();

        try {
            loadBalancedRestTemplate.postForObject("https://PSP/api/payments/callback", callback, Void.class);
            log.info("✅ PSP callback sent successfully for payment ID: {}", transaction.getPspPaymentId());
        } catch (Exception e) {
            log.error("❌ Failed to send PSP callback for payment ID {}: {}", transaction.getPspPaymentId(), e.getMessage(), e);
        }
    }
}