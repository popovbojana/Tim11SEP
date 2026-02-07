package com.sep.crypto.service;

import com.sep.crypto.dto.CoinGateCallback;
import com.sep.crypto.dto.GenericPaymentRequest;
import com.sep.crypto.dto.GenericPaymentResponse;

public interface CoinGateService {

    GenericPaymentResponse createOrder(GenericPaymentRequest request);
    void processCallback(CoinGateCallback callback);

}
