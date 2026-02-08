package com.sep.banksimulator.controller;

import com.sep.banksimulator.dto.CheckBalanceRequest;
import com.sep.banksimulator.dto.CheckBalanceResponse;
import com.sep.banksimulator.service.BankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping("/check-balance")
    public ResponseEntity<CheckBalanceResponse> checkBalance(@RequestBody CheckBalanceRequest request) {
        return ResponseEntity.ok(bankAccountService.checkBalance(request));
    }
}