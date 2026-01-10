package com.sep.webshop.controller;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import com.sep.webshop.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/init")
    public ResponseEntity<InitPaymentResponse> initPayment(@Valid @RequestBody CreateReservationRequest request,
                                                           Authentication authentication) {
        return new ResponseEntity<>(paymentService.initPayment(request, authentication.getName()), HttpStatus.OK);
    }

}
