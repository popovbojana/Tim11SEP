package com.sep.banksimulator.service;

import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.qr.ConfirmQrPaymentRequest;
import com.sep.banksimulator.dto.qr.QrImageResponse;

public interface BankPaymentService {

    GenericPaymentResponse initialize(GenericPaymentRequest request);
    QrImageResponse getQr(Long bankPaymentId);
    String confirmQr(Long bankPaymentId, ConfirmQrPaymentRequest request);
    String execute(Long bankPaymentId, ExecuteBankPaymentRequest request);

}