package com.sep.banksimulator.service;

import com.sep.banksimulator.dto.*;
import com.sep.banksimulator.dto.card.AuthorizeCardPaymentResponse;
import com.sep.banksimulator.dto.qr.ConfirmQrPaymentRequest;
import com.sep.banksimulator.dto.qr.QrImageResponse;

public interface BankPaymentService {

    InitBankPaymentResponse init(InitBankPaymentRequest request);

    InitBankPaymentResponse initQr(InitBankPaymentRequest request);

    QrImageResponse getQr(Long bankPaymentId);

    String confirmQr(Long bankPaymentId, ConfirmQrPaymentRequest request);

    AuthorizeCardPaymentResponse execute(Long bankPaymentId, ExecuteBankPaymentRequest request);
}