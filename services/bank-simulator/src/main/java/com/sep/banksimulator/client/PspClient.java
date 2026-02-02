package com.sep.banksimulator.client;

import com.sep.banksimulator.config.FeignHttpsConfig;
import com.sep.banksimulator.dto.BankCallbackRequest;
import com.sep.banksimulator.dto.PspPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "psp", url = "https://localhost:8081", configuration = FeignHttpsConfig.class)
public interface PspClient {

    @GetMapping("/api/payments/{pspPaymentId}")
    PspPaymentResponse getPayment(@PathVariable("pspPaymentId") Long pspPaymentId);

    @PostMapping("/api/bank/callback")
    void sendBankCallback(@RequestBody BankCallbackRequest request);

}