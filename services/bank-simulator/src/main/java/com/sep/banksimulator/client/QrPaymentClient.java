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

    private static final String QR_SERVICE_BASE = "http://localhost:8080/qr-payment";

    public GenerateQrResponse generate(GenerateQrRequest request) {
        return restTemplate.postForObject(QR_SERVICE_BASE + "/api/qr/generate", request, GenerateQrResponse.class);
    }

    public ValidateQrResponse validate(ValidateQrRequest request) {
        return restTemplate.postForObject(QR_SERVICE_BASE + "/api/qr/validate", request, ValidateQrResponse.class);
    }

}
