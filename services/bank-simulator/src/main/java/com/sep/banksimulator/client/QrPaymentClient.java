package com.sep.banksimulator.client;

import com.sep.banksimulator.config.FeignHttpsConfig;
import com.sep.banksimulator.dto.qr.GenerateQrRequest;
import com.sep.banksimulator.dto.qr.GenerateQrResponse;
import com.sep.banksimulator.dto.qr.ValidateQrRequest;
import com.sep.banksimulator.dto.qr.ValidateQrResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "qr-payment", url = "https://localhost:8083", configuration = FeignHttpsConfig.class)
public interface QrPaymentClient {

    @PostMapping("/api/qr/generate")
    GenerateQrResponse generate(@RequestBody GenerateQrRequest request);

    @PostMapping("/api/qr/validate")
    ValidateQrResponse validate(@RequestBody ValidateQrRequest request);

}