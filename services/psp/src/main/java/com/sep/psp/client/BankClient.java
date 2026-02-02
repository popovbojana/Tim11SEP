package com.sep.psp.client;

import com.sep.psp.config.FeignHttpsConfig;
import com.sep.psp.dto.payment.InitBankPaymentRequest;
import com.sep.psp.dto.payment.InitBankPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bank-simulator", url = "https://localhost:8084", configuration = FeignHttpsConfig.class)
public interface BankClient {

    @PostMapping("/api/payments/init")
    InitBankPaymentResponse initBankPayment(@RequestBody InitBankPaymentRequest request);

    @PostMapping("/api/payments/init/qr")
    InitBankPaymentResponse initBankQrPayment(@RequestBody InitBankPaymentRequest request);
}