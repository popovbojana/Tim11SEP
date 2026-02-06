package com.sep.banksimulator.client;

import com.sep.banksimulator.dto.GenericCallbackRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PspClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://psp/api/payments";

    public void sendCallback(GenericCallbackRequest request) {
        String url = BASE_URL + "/callback";
        restTemplate.postForObject(url, request, Void.class);
    }
}