package com.sep.banksimulator.controller;

import com.sep.banksimulator.dto.CheckBalanceRequest;
import com.sep.banksimulator.dto.CheckBalanceResponse;
import com.sep.banksimulator.service.BankPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankPaymentService bankPaymentService;

    @PostMapping("/check-balance")
    public ResponseEntity<CheckBalanceResponse> checkBalance(@RequestBody CheckBalanceRequest request) {
        return new ResponseEntity<>(bankPaymentService.checkBalance(request), HttpStatus.OK);
    }
}