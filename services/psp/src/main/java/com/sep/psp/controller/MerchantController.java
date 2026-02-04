package com.sep.psp.controller;

import com.sep.psp.dto.MerchantCreateRequest;
import com.sep.psp.dto.MerchantMethodsRequest;
import com.sep.psp.dto.MerchantResponse;
import com.sep.psp.entity.PaymentMethod;
import com.sep.psp.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<MerchantResponse> create(@Valid @RequestBody MerchantCreateRequest request) {
        return new ResponseEntity<>(merchantService.create(request.getMerchantKey()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping(value = "/{merchantKey}")
    public ResponseEntity<MerchantResponse> get(@PathVariable String merchantKey) {
        return new ResponseEntity<>(merchantService.getMerchant(merchantKey), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<Set<MerchantResponse>> getAll() {
        return new ResponseEntity<>(merchantService.getAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/{merchantKey}/methods")
    public ResponseEntity<Set<String>> getActiveMethods(@PathVariable String merchantKey) {
        return new ResponseEntity<>(merchantService.getActiveMethods(merchantKey), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(value = "/{merchantKey}/methods")
    public ResponseEntity<Set<String>> updateActiveMethods(
            @PathVariable String merchantKey,
            @Valid @RequestBody MerchantMethodsRequest request
    ) {
        return new ResponseEntity<>(merchantService.updateActiveMethods(merchantKey, request.getMethods())
                .stream()
                .map(PaymentMethod::getName)
                .collect(Collectors.toSet()), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        merchantService.remove(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}