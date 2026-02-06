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
    private static final String BASE_URL = "https://card-payment/api/cards";

    public AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest request) {
        String url = BASE_URL + "/authorize";
        return restTemplate.postForObject(url, request, AuthorizeCardPaymentResponse.class);
    }

}