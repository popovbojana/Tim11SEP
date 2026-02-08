package com.sep.webshop.service.impl;

import com.sep.webshop.dto.payment.GenericCallbackRequest;
import com.sep.webshop.entity.PaymentMethod;
import com.sep.webshop.entity.ReservationStatus;
import com.sep.webshop.exception.BadRequestException;
import com.sep.webshop.service.WebshopPaymentCallbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebshopPaymentCallbackServiceImpl implements WebshopPaymentCallbackService {

    private final RentalReservationServiceImpl reservationService;

    @Override
    @Transactional
    public void handle(GenericCallbackRequest request) {
        log.info("📨 Received payment callback — order: {}, status: {}, method: {}",
                request != null ? request.getMerchantOrderId() : "null",
                request != null ? request.getStatus() : "null",
                request != null ? request.getPaymentMethod() : "null");

        if (request == null || request.getMerchantOrderId() == null || request.getMerchantOrderId().isBlank()) {
            log.warn("❌ Invalid callback — missing merchant order ID");
            throw new BadRequestException("Missing merchant order id.");
        }

        ReservationStatus newStatus = mapReservationStatus(request.getStatus());
        PaymentMethod method = mapPaymentMethod(request.getPaymentMethod());
        log.info("⚙️ Mapped status: {} → {}, method: {} → {}", request.getStatus(), newStatus, request.getPaymentMethod(), method);

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

        log.info("✅ Callback processed — order: {}, reservation status: {}", request.getMerchantOrderId(), newStatus);
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