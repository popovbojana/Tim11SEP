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
    public ResponseEntity<InitPaymentResponse> initPayment(@Valid @RequestBody InitPaymentRequest request) {
        return new ResponseEntity<>(paymentService.initPayment(request), HttpStatus.CREATED);
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

    @GetMapping("/finalize/{externalPaymentId}")
    public ResponseEntity<Void> finalize(@PathVariable String externalPaymentId) {
        return ResponseEntity.status(302).headers(paymentService.finalize(externalPaymentId)).build();
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody GenericCallbackRequest request) {
        paymentService.handleCallback(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}