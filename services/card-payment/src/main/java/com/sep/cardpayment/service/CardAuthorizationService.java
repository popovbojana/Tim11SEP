package com.sep.cardpayment.service;

import com.sep.cardpayment.dto.AuthorizeCardPaymentRequest;
import com.sep.cardpayment.dto.AuthorizeCardPaymentResponse;

public interface CardAuthorizationService {

    AuthorizeCardPaymentResponse authorize(AuthorizeCardPaymentRequest req);

}