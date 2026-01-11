package com.sep.banksimulator.client;

import com.sep.banksimulator.dto.card.AuthorizeCardPaymentRequest;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class CardPaymentClient {

    private final RestTemplate restTemplate;

    public AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest request) {
        return restTemplate.postForObject("http://localhost:8080/card-payment/api/cards/authorize", request, AuthorizeCardPaymentResponse.class);
    }
}
