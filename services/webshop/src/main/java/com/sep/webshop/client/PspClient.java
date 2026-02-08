package com.sep.webshop.client;

import com.sep.webshop.config.PspConfig;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PspClient {

    private final RestTemplate restTemplate;
    private final PspConfig pspConfig;
    private static final String BASE_URL = "https://psp/api";

    public InitPaymentResponse initPayment(InitPaymentRequest request) {
        String url = BASE_URL + "/payments/init";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Merchant-Key", pspConfig.getMerchantKey());
        headers.set("X-Merchant-Password", pspConfig.getMerchantPassword());
        headers.set("Content-Type", "application/json");

        HttpEntity<InitPaymentRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.postForObject(url, entity, InitPaymentResponse.class);
    }
}