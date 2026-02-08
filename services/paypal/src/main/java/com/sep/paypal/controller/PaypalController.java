package com.sep.paypal.controller;

import com.sep.paypal.dto.GenericPaymentRequest;
import com.sep.paypal.dto.GenericPaymentResponse;
import com.sep.paypal.service.PaypalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;

    @PostMapping("/init")
    public ResponseEntity<GenericPaymentResponse> createOrder(@RequestBody GenericPaymentRequest request) {
        return new ResponseEntity<>(paypalService.createOrder(request), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmPayment(@RequestParam("token") String token) {
        String redirectUrl = paypalService.captureOrder(token);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/cancel")
    public ResponseEntity<Void> cancelPayment(@RequestParam("token") String token) {
        String redirectUrl = paypalService.cancelOrder(token);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}