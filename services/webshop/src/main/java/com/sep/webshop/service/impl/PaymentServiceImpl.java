package com.sep.webshop.service.impl;

import com.sep.webshop.client.PspClient;
import com.sep.webshop.config.PspConfig;
import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.dto.payment.InitPaymentRequest;
import com.sep.webshop.dto.payment.InitPaymentResponse;
import com.sep.webshop.service.PaymentService;
import com.sep.webshop.service.RentalReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PspConfig pspConfig;
    private final PspClient pspClient;
    private final RentalReservationService reservationService;

    @Override
    @Transactional
    public InitPaymentResponse initPayment(CreateReservationRequest reservationRequest, String customerEmail) {
        log.info("Initiating payment for customer: {}", customerEmail);

        String merchantOrderId = UUID.randomUUID().toString();
        log.info("Generated merchant order ID: {}", merchantOrderId);

        ReservationDTO pendingReservation =
                reservationService.create(reservationRequest, customerEmail, merchantOrderId);
        log.info("Reservation created — ID: {}, total: {} €", pendingReservation.getId(), pendingReservation.getTotalPrice());

        InitPaymentRequest request = InitPaymentRequest.builder()
                .merchantKey(pspConfig.getMerchantKey())
                .merchantOrderId(merchantOrderId)
                .reservationId(pendingReservation.getId())
                .amount(pendingReservation.getTotalPrice())
                .currency(pendingReservation.getCurrency())
                .build();

        log.info("Sending init payment request to PSP — amount: {} €", pendingReservation.getTotalPrice());

        try {
            InitPaymentResponse response = pspClient.initPayment(request);
            log.info("PSP responded — payment ID: {}, redirect: {}", response.getPaymentId(), response.getRedirectUrl());
            return response;
        } catch (Exception e) {
            log.error("PSP init payment failed for order {}: {}", merchantOrderId, e.getMessage(), e);
            throw e;
        }
    }
}