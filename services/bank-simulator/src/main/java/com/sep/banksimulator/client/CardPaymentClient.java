package com.sep.banksimulator.client;

import com.sep.banksimulator.config.FeignHttpsConfig;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentRequest;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "card-payment", url = "https://localhost:8082", configuration = FeignHttpsConfig.class)
public interface CardPaymentClient {

    @PostMapping("/api/cards/authorize")
    AuthorizeCardPaymentResponse authorize(@RequestBody AuthorizeCardPaymentRequest request);

}