package com.sep.psp.controller;

import com.sep.psp.dto.payment.*;
import com.sep.psp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/init")
    public ResponseEntity<InitPaymentResponse> initPayment(
            @Valid @RequestBody InitPaymentRequest request,
            @RequestHeader("X-Merchant-Key") String merchantKey,
            @RequestHeader("X-Merchant-Password") String merchantPassword
    ) {
        return new ResponseEntity<>(
                paymentService.initPayment(request, merchantKey, merchantPassword),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return new ResponseEntity<>(paymentService.getPayment(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<StartPaymentResponse> startPayment(
            @PathVariable Long id,
            @RequestParam String methodName
    ) {
        return new ResponseEntity<>(paymentService.startPayment(id, methodName), HttpStatus.OK);
    }

    @GetMapping("/finalize/{id}")
    public ResponseEntity<Void> finalize(@PathVariable Long id) {
        return ResponseEntity.status(302).headers(paymentService.finalize(id)).build();
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@Valid @RequestBody GenericCallbackRequest request) {
        paymentService.handleCallback(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}