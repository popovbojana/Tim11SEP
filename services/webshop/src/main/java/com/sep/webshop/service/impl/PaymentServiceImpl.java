package com.sep.webshop.service.impl;

import com.sep.webshop.client.PspClient;
import com.sep.webshop.config.PspConfig;
import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import com.sep.webshop.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PspConfig pspConfig;
    private final PspClient pspClient;
    private final RentalReservationServiceImpl reservationService;

    @Override
    @Transactional
    public InitPaymentResponse initPayment(CreateReservationRequest reservationRequest, String customerEmail) {

        String merchantOrderId = UUID.randomUUID().toString();

        ReservationDTO pendingReservation =
                reservationService.create(reservationRequest, customerEmail, merchantOrderId);

        String successUrl =
                "https://localhost:4200/payment/success/" + pendingReservation.getId();
        String failedUrl =
                "https://localhost:4200/payment/failed/" + pendingReservation.getId();
        String errorUrl =
                "https://localhost:4200/payment/error/" + pendingReservation.getId();

        InitPaymentRequest request = InitPaymentRequest.builder()
                .merchantKey(pspConfig.getMerchantKey())
                .merchantOrderId(merchantOrderId)
                .amount(pendingReservation.getTotalPrice())
                .currency("€")
                .successUrl(successUrl)
                .failedUrl(failedUrl)
                .errorUrl(errorUrl)
                .build();

        return pspClient.initPayment(request);
    }

}
