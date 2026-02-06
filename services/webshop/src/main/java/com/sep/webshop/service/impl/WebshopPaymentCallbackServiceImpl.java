package com.sep.webshop.service.impl;

import com.sep.webshop.dto.payment.GenericCallbackRequest;
import com.sep.webshop.entity.PaymentMethod;
import com.sep.webshop.entity.ReservationStatus;
import com.sep.webshop.exception.BadRequestException;
import com.sep.webshop.service.WebshopPaymentCallbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WebshopPaymentCallbackServiceImpl implements WebshopPaymentCallbackService {

    private final RentalReservationServiceImpl reservationService;

    @Override
    @Transactional
    public void handle(GenericCallbackRequest request) {
        if (request == null || request.getMerchantOrderId() == null || request.getMerchantOrderId().isBlank()) {
            throw new BadRequestException("Missing merchant order id.");
        }

        ReservationStatus newStatus = mapReservationStatus(request.getStatus());
        PaymentMethod method = mapPaymentMethod(request.getPaymentMethod());

        Instant paidAt = request.getAcquirerTimestamp();
        String reference = request.getGlobalTransactionId();

        reservationService.updateFromPaymentCallback(
                request.getMerchantOrderId(),
                request.getPspPaymentId(),
                newStatus,
                method,
                reference,
                paidAt
        );
    }

    private ReservationStatus mapReservationStatus(String paymentStatus) {
        if (paymentStatus == null) return ReservationStatus.CANCELED;

        if ("SUCCESS".equalsIgnoreCase(paymentStatus)) return ReservationStatus.CONFIRMED;
        if ("FAILED".equalsIgnoreCase(paymentStatus)) return ReservationStatus.CANCELED;
        if ("CANCELED".equalsIgnoreCase(paymentStatus)) return ReservationStatus.EXPIRED;
        return ReservationStatus.CANCELED;
    }

    private PaymentMethod mapPaymentMethod(String m) {
        if (m == null) return null;
        if ("CARD".equalsIgnoreCase(m)) return PaymentMethod.CARD;
        if ("QR".equalsIgnoreCase(m)) return PaymentMethod.QR;
        if ("PAYPAL".equalsIgnoreCase(m)) return PaymentMethod.PAYPAL;
        if ("CRYPTO".equalsIgnoreCase(m)) return PaymentMethod.CRYPTO;
        return null;
    }
}
