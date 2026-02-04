package com.sep.psp.service;

import com.sep.psp.dto.payment.*;
import org.springframework.http.HttpHeaders;

public interface PaymentService {

    InitPaymentResponse initPayment(InitPaymentRequest request);
    StartPaymentResponse startCardPayment(Long paymentId);
    StartPaymentResponse startQrPayment(Long paymentId);
    PaymentResponse getPayment(Long id);
    void handleBankCallback(BankCallbackRequest request);
    HttpHeaders finalize(Long bankPaymentId);

}
