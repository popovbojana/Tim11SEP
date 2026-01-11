package com.sep.banksimulator.controller;

import com.sep.banksimulator.dto.ExecuteBankPaymentRequest;
import com.sep.banksimulator.dto.InitBankPaymentRequest;
import com.sep.banksimulator.dto.InitBankPaymentResponse;
import com.sep.banksimulator.dto.RedirectResponse;
import com.sep.banksimulator.service.BankPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class BankPaymentController {

    private final BankPaymentService bankPaymentService;

    @PostMapping("/init")
    public ResponseEntity<InitBankPaymentResponse> init(
            @Valid @RequestBody InitBankPaymentRequest request
    ) {
        return new ResponseEntity<>(bankPaymentService.init(request), HttpStatus.OK);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<RedirectResponse> execute(
            @PathVariable Long id,
            @RequestBody ExecuteBankPaymentRequest request
    ) {
        return new ResponseEntity<>(new RedirectResponse(bankPaymentService.execute(id, request.isSuccess())), HttpStatus.OK);
    }

}
