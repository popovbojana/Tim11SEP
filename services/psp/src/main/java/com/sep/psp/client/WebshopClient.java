package com.sep.psp.client;

import com.sep.psp.dto.payment.WebshopPaymentCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WebshopClient {

    private final RestTemplate restTemplate;

    public void sendPaymentCallback(WebshopPaymentCallbackRequest request) {
        restTemplate.postForObject("http://localhost:8080/webshop/api/payments/callback", request, Void.class);
    }

}
