package com.sep.webshop.service;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;

public interface PaymentService {

    InitPaymentResponse initPayment(CreateReservationRequest reservationRequest, String customerEmail);

}
