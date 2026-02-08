package com.sep.psp.controller;

import com.sep.psp.dto.paymentMethod.PaymentMethodRequest;
import com.sep.psp.dto.paymentMethod.PaymentMethodResponse;
import com.sep.psp.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<Set<PaymentMethodResponse>> getAll() {
        return new ResponseEntity<>(paymentMethodService.findAll(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentMethodResponse> add(@Valid @RequestBody PaymentMethodRequest method) {
        return new ResponseEntity<>(paymentMethodService.add(method), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.remove(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(value = "/{id}")
    public ResponseEntity<PaymentMethodResponse> update(
            @PathVariable Long id,
            @RequestBody PaymentMethodRequest methodRequest) {
        return new ResponseEntity<>(paymentMethodService.update(id, methodRequest), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(value = "/{id}")
    public ResponseEntity<PaymentMethodResponse> findById(@PathVariable Long id) {
        return new ResponseEntity<>(paymentMethodService.findById(id), HttpStatus.OK);
    }

}