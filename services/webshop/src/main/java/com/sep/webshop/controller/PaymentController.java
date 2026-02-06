package com.sep.webshop.controller;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import com.sep.webshop.dto.payment.GenericCallbackRequest;
import com.sep.webshop.service.impl.PaymentServiceImpl;
import com.sep.webshop.service.impl.WebshopPaymentCallbackServiceImpl;
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

    private final PaymentServiceImpl paymentService;

    private final WebshopPaymentCallbackServiceImpl callbackService;

    @PostMapping("/init")
    public ResponseEntity<InitPaymentResponse> initPayment(@Valid @RequestBody CreateReservationRequest request,
                                                           Authentication authentication) {
        return new ResponseEntity<>(paymentService.initPayment(request, authentication.getName()), HttpStatus.OK);
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody GenericCallbackRequest request) {
        callbackService.handle(request);
        return ResponseEntity.ok().build();
    }

}
