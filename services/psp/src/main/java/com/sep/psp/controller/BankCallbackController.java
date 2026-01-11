package com.sep.psp.controller;

import com.sep.psp.entity.Payment;
import com.sep.psp.entity.PaymentStatus;
import com.sep.psp.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankCallbackController {

    private final PaymentRepository paymentRepository;

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody Object bankPayment) {
        var map = (java.util.Map<?, ?>) bankPayment;

        Long pspPaymentId = ((Number) map.get("pspPaymentId")).longValue();
        String status = (String) map.get("status");

        Payment payment = paymentRepository.findById(pspPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setStatus(
                "SUCCESS".equals(status) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED
        );

        paymentRepository.save(payment);

        return ResponseEntity.ok().build();
    }
}
