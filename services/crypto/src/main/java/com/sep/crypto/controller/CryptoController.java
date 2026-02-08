package com.sep.crypto.controller;

import com.sep.crypto.dto.CoinGateCallback;
import com.sep.crypto.dto.GenericPaymentRequest;
import com.sep.crypto.dto.GenericPaymentResponse;
import com.sep.crypto.service.CoinGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class CryptoController {

    private final CoinGateService coinGateService;

    @PostMapping("/init")
    public ResponseEntity<GenericPaymentResponse> createOrder(@RequestBody GenericPaymentRequest request) {
        return new ResponseEntity<>(coinGateService.createOrder(request), HttpStatus.OK);
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody CoinGateCallback callback) {
        coinGateService.processCallback(callback);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}