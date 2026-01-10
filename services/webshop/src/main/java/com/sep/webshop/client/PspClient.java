package com.sep.webshop.client;

import com.sep.webshop.config.PspConfig;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PspClient {

    private final RestTemplate restTemplate;

    private final PspConfig pspConfig;

    public InitPaymentResponse initPayment(InitPaymentRequest request) {
        String url = pspConfig.getBaseUrl() + "/api/payments/init";

        return restTemplate.postForObject(
                url,
                request,
                InitPaymentResponse.class
        );
    }

}
