package com.sep.cardpayment.controller;

import com.sep.cardpayment.dto.AuthorizeCardPaymentRequest;
import com.sep.cardpayment.dto.AuthorizeCardPaymentResponse;
import com.sep.cardpayment.service.CardAuthorizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardPaymentController {

    private final CardAuthorizationService cardAuthorizationService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizeCardPaymentResponse> authorize(@Valid @RequestBody AuthorizeCardPaymentRequest request) {
        return new ResponseEntity<>(cardAuthorizationService.authorize(request), HttpStatus.OK);
    }

}
