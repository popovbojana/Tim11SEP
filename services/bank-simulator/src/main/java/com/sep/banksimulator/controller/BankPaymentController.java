package com.sep.banksimulator.controller;

import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.qr.ConfirmQrPaymentRequest;
import com.sep.banksimulator.dto.qr.QrImageResponse;
import com.sep.banksimulator.service.BankPaymentService;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
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
    public ResponseEntity<GenericPaymentResponse> init(@Valid @RequestBody GenericPaymentRequest request) {
        return new ResponseEntity<>(bankPaymentService.initialize(request), HttpStatus.OK);
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<QrImageResponse> getQr(@PathVariable Long id) {
        return new ResponseEntity<>(bankPaymentService.getQr(id), HttpStatus.OK);
    }

    @PostMapping("/{id}/qr/confirm")
    public ResponseEntity<RedirectResponse> confirmQr(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmQrPaymentRequest request
    ) {
        return new ResponseEntity<>(new RedirectResponse(bankPaymentService.confirmQr(id, request)), HttpStatus.OK);
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<AuthorizeCardPaymentResponse> execute(
            @PathVariable Long id,
            @Valid @RequestBody ExecuteBankPaymentRequest request
    ) {
        AuthorizeCardPaymentResponse response = bankPaymentService.execute(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}