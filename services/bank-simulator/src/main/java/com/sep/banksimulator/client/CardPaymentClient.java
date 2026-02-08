package com.sep.banksimulator.client;

import com.sep.banksimulator.dto.card.AuthorizeCardPaymentRequest;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import com.sep.banksimulator.dto.card.CardPaymentStatus;
import com.sep.banksimulator.dto.FailureReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardPaymentClient {

    private final RestTemplate restTemplate;

    private static final String BANK_AUTH_URL = "http://localhost:8080/card-payment/api/cards/authorize";

    public AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest request) {
        try {
            log.info("Initiating card authorization for amount: {} {}", request.getAmount(), request.getCurrency());

            AuthorizeCardPaymentResponse response = restTemplate.postForObject(BANK_AUTH_URL, request, AuthorizeCardPaymentResponse.class);

            if (response != null) {
                log.info("Bank responded with status: {}", response.getStatus());
            }

            return response;

        } catch (HttpClientErrorException e) {
            log.error("Client error during authorization: {}", e.getStatusCode());
            return AuthorizeCardPaymentResponse.builder()
                    .status(CardPaymentStatus.FAILED)
                    .reason(FailureReason.INSUFFICIENT_FUNDS)
                    .acquirerTimestamp(Instant.now())
                    .build();

        } catch (HttpServerErrorException e) {
            log.error("Server error within the bank system: {}", e.getStatusCode());
            return AuthorizeCardPaymentResponse.builder()
                    .status(CardPaymentStatus.FAILED)
                    .reason(FailureReason.SYSTEM_ERROR)
                    .acquirerTimestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Unexpected communication error: {}", e.getMessage());
            return AuthorizeCardPaymentResponse.builder()
                    .status(CardPaymentStatus.FAILED)
                    .reason(FailureReason.SYSTEM_ERROR)
                    .acquirerTimestamp(Instant.now())
                    .build();
        }
    }
}