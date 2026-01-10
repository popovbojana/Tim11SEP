package com.sep.banksimulator.controller;

import com.sep.banksimulator.dto.InitBankPaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/payments")
public class BankPaymentController {

    private static final AtomicLong SEQ = new AtomicLong(1);

    @PostMapping("/init")
    public ResponseEntity<InitBankPaymentResponse> init() {
        long id = SEQ.getAndIncrement();

        String redirectUrl = "http://localhost:8080/bank-simulator/bank/checkout/" + id;

        return ResponseEntity.ok(
                InitBankPaymentResponse.builder()
                        .bankPaymentId(id)
                        .redirectUrl(redirectUrl)
                        .build()
        );
    }
}
