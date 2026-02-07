package com.sep.paypal.service;

import com.sep.paypal.dto.GenericPaymentRequest;
import com.sep.paypal.dto.GenericPaymentResponse;

public interface PaypalService {

    GenericPaymentResponse createOrder(GenericPaymentRequest genericRequest);
    String captureOrder(String paypalOrderId);

}
