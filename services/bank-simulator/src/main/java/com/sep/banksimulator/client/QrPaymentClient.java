package com.sep.banksimulator.client;

import com.sep.banksimulator.dto.qr.GenerateQrRequest;
import com.sep.banksimulator.dto.qr.GenerateQrResponse;
import com.sep.banksimulator.dto.qr.ValidateQrRequest;
import com.sep.banksimulator.dto.qr.ValidateQrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class QrPaymentClient {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://qr-payment/api/qr";

    public GenerateQrResponse generate(GenerateQrRequest request) {
        String url = BASE_URL + "/generate";
        return restTemplate.postForObject(url, request, GenerateQrResponse.class);
    }

    public ValidateQrResponse validate(ValidateQrRequest request) {
        String url = BASE_URL + "/validate";
        return restTemplate.postForObject(url, request, ValidateQrResponse.class);
    }
}