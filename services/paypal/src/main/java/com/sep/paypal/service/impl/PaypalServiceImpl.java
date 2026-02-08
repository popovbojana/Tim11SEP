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
        String token = paypalAuthService.getAccessToken();

        PaypalOrderRequest request = new PaypalOrderRequest();
        request.setIntent("CAPTURE");

        PaypalOrderRequest.Amount orderAmount = new PaypalOrderRequest.Amount();
        String currency = genericRequest.getCurrency();
        if ("€".equals(currency)) currency = "EUR";
        else if ("$".equals(currency)) currency = "USD";

        orderAmount.setCurrency_code(currency);
        orderAmount.setValue(String.format("%.2f", genericRequest.getAmount()).replace(",", "."));

        PaypalOrderRequest.PurchaseUnit unit = new PaypalOrderRequest.PurchaseUnit();
        unit.setAmount(orderAmount);
        request.setPurchase_units(List.of(unit));

        PaypalOrderRequest.ApplicationContext context = new PaypalOrderRequest.ApplicationContext();
        context.setReturn_url("https://localhost:8086/api/payments/confirm");
        // FIKS: Sada gadjamo nas cancel endpoint
        context.setCancel_url("https://localhost:8086/api/payments/cancel");
        request.setApplication_context(context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PaypalOrderRequest> entity = new HttpEntity<>(request, headers);

        PaypalOrderResponse response = externalRestTemplate.postForObject(
                "https://api-m.sandbox.paypal.com/v2/checkout/orders",
                entity,
                PaypalOrderResponse.class
        );

        if (response != null) {
            PaypalTransaction transaction = PaypalTransaction.builder()
                    .pspPaymentId(genericRequest.getPspPaymentId())
                    .paypalOrderId(response.getId())
                    .status("CREATED")
                    .amount(genericRequest.getAmount())
                    .merchantOrderId(genericRequest.getMetadata().get("merchantOrderId"))
                    .merchantKey(genericRequest.getMetadata().get("merchantKey"))
                    .currency(currency)
                    .createdAt(Instant.now())
                    .build();

            paypalTransactionRepository.save(transaction);

            String approveUrl = response.getLinks().stream()
                    .filter(link -> link.getRel().equals("approve"))
                    .findFirst()
                    .map(PaypalOrderResponse.Link::getHref)
                    .orElse("");

            return GenericPaymentResponse.builder()
                    .redirectUrl(approveUrl)
                    .externalPaymentId(response.getId())
                    .build();
        }
        throw new RuntimeException("PayPal order creation failed");
    }

    @Override
    public String captureOrder(String paypalOrderId) {
        PaypalTransaction transaction = paypalTransactionRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        try {
            String token = paypalAuthService.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            ResponseEntity<Map> response = externalRestTemplate.postForEntity(
                    "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + paypalOrderId + "/capture",
                    new HttpEntity<>("{}", headers),
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                transaction.setStatus("SUCCESS");
                paypalTransactionRepository.save(transaction);
                notifyPsp(transaction, "SUCCESS");
            } else {
                transaction.setStatus("FAILED");
                paypalTransactionRepository.save(transaction);
                notifyPsp(transaction, "FAILED");
            }
        } catch (Exception e) {
            log.error("PayPal capture error: {}", e.getMessage());
            transaction.setStatus("ERROR");
            paypalTransactionRepository.save(transaction);
            notifyPsp(transaction, "FAILED");
        }
        return PSP_FINALIZE_URL + transaction.getPspPaymentId();
    }

    @Override
    public String cancelOrder(String paypalOrderId) {
        PaypalTransaction transaction = paypalTransactionRepository.findByPaypalOrderId(paypalOrderId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus("CANCELED");
        paypalTransactionRepository.save(transaction);

        notifyPsp(transaction, "CANCELED");

        return PSP_FINALIZE_URL + transaction.getPspPaymentId();
    }

    private void notifyPsp(PaypalTransaction transaction, String status) {
        GenericCallbackRequest callback = GenericCallbackRequest.builder()
                .pspPaymentId(transaction.getPspPaymentId())
                .merchantOrderId(transaction.getMerchantOrderId())
                .amount(transaction.getAmount())
                .status(status)
                .externalPaymentId(transaction.getPaypalOrderId())
                .globalTransactionId(transaction.getPaypalOrderId())
                .stan(transaction.getPaypalOrderId())
                .paymentMethod("PAYPAL")
                .acquirerTimestamp(Instant.now())
                .pspTimestamp(transaction.getCreatedAt())
                .build();

        try {
            loadBalancedRestTemplate.postForObject("https://PSP/api/payments/callback", callback, Void.class);
        } catch (Exception e) {
            log.error("Failed to notify PSP: {}", e.getMessage());
        }
    }
}