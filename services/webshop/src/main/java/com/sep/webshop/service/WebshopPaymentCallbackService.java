package com.sep.webshop.service;

import com.sep.webshop.dto.payment.GenericCallbackRequest;
import com.sep.webshop.entity.PaymentMethod;
import com.sep.webshop.entity.ReservationStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WebshopPaymentCallbackService {

    private final RentalReservationService reservationService;

    @Transactional
    public void handle(GenericCallbackRequest request) {
        if (request == null || request.getMerchantOrderId() == null || request.getMerchantOrderId().isBlank()) {
            throw new IllegalArgumentException("Missing merchantOrderId");
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
