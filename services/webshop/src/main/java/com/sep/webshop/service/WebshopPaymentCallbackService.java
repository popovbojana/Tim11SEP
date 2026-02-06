package com.sep.webshop.service;

import com.sep.webshop.dto.payment.GenericCallbackRequest;

public interface WebshopPaymentCallbackService {

    void handle(GenericCallbackRequest request);

}
