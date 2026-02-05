package com.sep.psp.controller;

import com.sep.psp.dto.payment.BankCallbackRequest;
import com.sep.psp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankCallbackController {

    private final PaymentService paymentService;

//    @PostMapping("/callback")
//    public ResponseEntity<Void> callback(@RequestBody BankCallbackRequest request) {
//        paymentService.handleBankCallback(request);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
}
