package com.sep.psp.controller;

import com.sep.psp.dto.payment.InitPaymentRequest;
import com.sep.psp.dto.payment.InitPaymentResponse;
import com.sep.psp.dto.payment.PaymentResponse;
import com.sep.psp.dto.payment.StartPaymentResponse;
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
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping("/{id}/start/card")
    public ResponseEntity<StartPaymentResponse> startCardPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.startCardPayment(id));
    }
}
