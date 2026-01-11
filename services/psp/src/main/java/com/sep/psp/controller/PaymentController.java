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
        return new ResponseEntity<>(paymentService.getPayment(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/start/card")
    public ResponseEntity<StartPaymentResponse> startCardPayment(@PathVariable Long id) {
        return new ResponseEntity<>(paymentService.startCardPayment(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/start/qr")
    public ResponseEntity<StartPaymentResponse> startQrPayment(@PathVariable Long id) {
        return new ResponseEntity<>(paymentService.startQrPayment(id), HttpStatus.OK);
    }

    @GetMapping("/finalize/{bankPaymentId}")
    public ResponseEntity<Void> finalizeByBankPaymentId(@PathVariable Long bankPaymentId) {
        return ResponseEntity.status(302).headers(paymentService.finalize(bankPaymentId)).build();
    }
}
