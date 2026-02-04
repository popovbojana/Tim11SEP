package com.sep.psp.service;

import com.sep.psp.dto.paymentMethod.PaymentMethodRequest;
import com.sep.psp.dto.paymentMethod.PaymentMethodResponse;

import java.util.Set;

public interface PaymentMethodService {

    Set<PaymentMethodResponse> findAll();
    PaymentMethodResponse add(PaymentMethodRequest methodRequest);
    void remove(Long id);
    PaymentMethodResponse update(Long id, PaymentMethodRequest methodRequest);
    PaymentMethodResponse findById(Long id);

}
