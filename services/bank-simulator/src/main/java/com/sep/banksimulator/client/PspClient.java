package com.sep.banksimulator.client;

import com.sep.banksimulator.dto.PspPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PspClient {

    private final RestTemplate restTemplate;

    public PspPaymentResponse getPayment(Long pspPaymentId) {
        String url = "http://localhost:8080/psp/api/payments/" + pspPaymentId;
        return restTemplate.getForObject(url, PspPaymentResponse.class);
    }

}
