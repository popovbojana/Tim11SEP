package com.sep.psp.client;

import com.sep.psp.config.BankConfig;
import com.sep.psp.dto.payment.InitBankPaymentRequest;
import com.sep.psp.dto.payment.InitBankPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class BankClient {

    private final RestTemplate restTemplate;

    private final BankConfig bankConfig;

    public InitBankPaymentResponse initBankPayment(InitBankPaymentRequest request) {
        String url = bankConfig.getBaseUrl() + "/api/payments/init";
        return restTemplate.postForObject(url, request, InitBankPaymentResponse.class);
    }

}
