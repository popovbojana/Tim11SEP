package com.sep.paypal.service.impl;

import com.sep.paypal.service.PaypalAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class PaypalAuthServiceImpl implements PaypalAuthService {

    private final RestTemplate restTemplate;

    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.secret.key}")
    private String secret;

    private static final String AUTH_URL = "https://api-m.sandbox.paypal.com/v1/oauth2/token";

    public PaypalAuthServiceImpl(@Qualifier("externalRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getAccessToken() {
        log.info("🔐 Requesting PayPal access token...");

        String auth = clientId + ":" + secret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(AUTH_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("✅ PayPal access token obtained successfully");
                return (String) response.getBody().get("access_token");
            }

            log.error("❌ PayPal auth failed — status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("❌ PayPal auth request failed: {}", e.getMessage(), e);
            throw e;
        }

        throw new RuntimeException("Greška pri dobijanju PayPal access tokena!");
    }
}