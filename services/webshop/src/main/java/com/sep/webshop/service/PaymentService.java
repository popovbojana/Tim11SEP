package com.sep.webshop.service;

import com.sep.webshop.client.PspClient;
import com.sep.webshop.config.PspConfig;
import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PspConfig pspConfig;
    private final PspClient pspClient;
    private final RentalReservationService reservationService;

    public InitPaymentResponse initPayment(CreateReservationRequest reservationRequest, String customerEmail) {

        ReservationDTO pendingReservation =
                reservationService.create(reservationRequest, customerEmail);

        String successUrl =
                "http://localhost:4200/payment/success?reservationId=" + pendingReservation.getId();
        String failedUrl =
                "http://localhost:4200/payment/failed?reservationId=" + pendingReservation.getId();
        String errorUrl =
                "http://localhost:4200/payment/error?reservationId=" + pendingReservation.getId();

        InitPaymentRequest request = InitPaymentRequest.builder()
                .merchantKey(pspConfig.getMerchantKey())
                .merchantOrderId(UUID.randomUUID().toString())
                .amount(pendingReservation.getTotalPrice())
                .successUrl(successUrl)
                .failedUrl(failedUrl)
                .errorUrl(errorUrl)
                .build();

        return pspClient.initPayment(request);
    }
}
