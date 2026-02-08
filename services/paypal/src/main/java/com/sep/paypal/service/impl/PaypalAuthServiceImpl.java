package com.sep.paypal.service.impl;

import com.sep.paypal.service.PaypalAuthService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class PaypalAuthServiceImpl implements PaypalAuthService {

    private final RestTemplate restTemplate;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.secret.key}")
    private String secret;

    @Value("${paypal.auth.url}")
    private String paypalAuthUrl;

    public PaypalAuthServiceImpl(@Qualifier("externalRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getAccessToken() {
        String auth = clientId + ":" + secret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(paypalAuthUrl, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String token = (String) response.getBody().get("access_token");
                if (token != null) {
                    return token;
                }
            }
            throw new RuntimeException("Failed to retrieve PayPal access token: Empty response or missing token.");
        } catch (Exception e) {
            throw new RuntimeException("PayPal authentication failed: " + e.getMessage());
        }
    }
}