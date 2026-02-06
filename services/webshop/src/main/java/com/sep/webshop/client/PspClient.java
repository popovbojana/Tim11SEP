package com.sep.webshop.client;

import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PspClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://psp/api/payments";

    public InitPaymentResponse initPayment(InitPaymentRequest request) {
        String url = BASE_URL + "/init";
        return restTemplate.postForObject(url, request, InitPaymentResponse.class);
    }
}