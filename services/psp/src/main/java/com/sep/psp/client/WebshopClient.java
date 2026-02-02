package com.sep.psp.client;

import com.sep.psp.config.FeignHttpsConfig;
import com.sep.psp.dto.payment.WebshopPaymentCallbackRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "webshop", url = "https://localhost:8085", configuration = FeignHttpsConfig.class)
public interface WebshopClient {

    @PostMapping("/api/payments/callback")
    void sendPaymentCallback(@RequestBody WebshopPaymentCallbackRequest request);

}