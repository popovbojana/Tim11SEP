package com.sep.psp.service;

import com.sep.psp.dto.payment.*;
import org.springframework.http.HttpHeaders;

public interface PaymentService {

    InitPaymentResponse initPayment(InitPaymentRequest request, String key, String rawPassword);
    PaymentResponse getPayment(Long id);
    StartPaymentResponse startPayment(Long paymentId, String methodName);
    void handleCallback(GenericCallbackRequest request);
    HttpHeaders finalize(Long id);

}