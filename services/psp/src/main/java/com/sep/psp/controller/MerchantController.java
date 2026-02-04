package com.sep.psp.controller;

import com.sep.psp.dto.merchant.MerchantCreateRequest;
import com.sep.psp.dto.merchant.MerchantResponse;
import com.sep.psp.dto.merchant.MerchantUpdateRequest;
import com.sep.psp.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<MerchantResponse> create(@Valid @RequestBody MerchantCreateRequest request) {
        return new ResponseEntity<>(merchantService.create(request), HttpStatus.CREATED);
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

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping(value = "/{merchantKey}")
    public ResponseEntity<MerchantResponse> update(
            @PathVariable String merchantKey,
            @Valid @RequestBody MerchantUpdateRequest request
    ) {
        return new ResponseEntity<>(merchantService.update(merchantKey, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        merchantService.remove(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/{merchantKey}/methods")
    public ResponseEntity<Set<String>> getActiveMethods(@PathVariable String merchantKey) {
        return new ResponseEntity<>(merchantService.getActiveMethods(merchantKey), HttpStatus.OK);
    }
}