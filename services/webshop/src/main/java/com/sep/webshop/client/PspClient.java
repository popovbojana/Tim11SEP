package com.sep.webshop.client;

import com.sep.webshop.config.FeignHttpsConfig;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "psp-client", url = "https://localhost:8080/psp", configuration = FeignHttpsConfig.class)
public interface PspClient {

    @PostMapping("/api/payments/init")
    InitPaymentResponse initPayment(@RequestBody InitPaymentRequest request);

}