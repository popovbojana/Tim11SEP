package com.sep.qrpayment.controller;

import com.sep.qrpayment.dto.GenerateQrRequest;
import com.sep.qrpayment.dto.GenerateQrResponse;
import com.sep.qrpayment.dto.ValidateQrRequest;
import com.sep.qrpayment.dto.ValidateQrResponse;
import com.sep.qrpayment.service.QrCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateQrResponse> generate(@Valid @RequestBody GenerateQrRequest request) {
        return new ResponseEntity<>(qrCodeService.generate(request), HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateQrResponse> validate(@Valid @RequestBody ValidateQrRequest request) {
        return new ResponseEntity<>(qrCodeService.validate(request), HttpStatus.OK);
    }

}
