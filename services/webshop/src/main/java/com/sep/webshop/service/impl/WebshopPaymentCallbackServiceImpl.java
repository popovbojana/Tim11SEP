package com.sep.webshop.service.impl;

import com.sep.webshop.dto.payment.GenericCallbackRequest;
import com.sep.webshop.entity.ReservationStatus;
import com.sep.webshop.exception.BadRequestException;
import com.sep.webshop.service.RentalReservationService;
import com.sep.webshop.service.WebshopPaymentCallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebshopPaymentCallbackServiceImpl implements WebshopPaymentCallbackService {

    private final RentalReservationService reservationService;

    @Override
    @Transactional
    public void handle(GenericCallbackRequest request) {
        log.info("Received payment callback — order: {}, status: {}, method: {}",
                request != null ? request.getMerchantOrderId() : "null",
                request != null ? request.getStatus() : "null",
                request != null ? request.getPaymentMethod() : "null");

        if (request == null || request.getMerchantOrderId() == null || request.getMerchantOrderId().isBlank()) {
            log.warn("Invalid callback — missing merchant order ID");
            throw new BadRequestException("Missing merchant order id.");
        }

        ReservationStatus newStatus = mapReservationStatus(request.getStatus());
        log.info("Mapped status: {} → {}, method: {}", request.getStatus(), newStatus, request.getPaymentMethod());

        reservationService.updateFromPaymentCallback(
                request.getMerchantOrderId(),
                request.getPspPaymentId(),
                newStatus,
                request.getPaymentMethod(),
                request.getGlobalTransactionId(),
                request.getAcquirerTimestamp() != null ? request.getAcquirerTimestamp() : Instant.now()
        );

        log.info("Callback processed — order: {}, reservation status: {}", request.getMerchantOrderId(), newStatus);
    }

    private ReservationStatus mapReservationStatus(String paymentStatus) {
        if (paymentStatus == null) return ReservationStatus.FAILED;

        return switch (paymentStatus.toUpperCase()) {
            case "SUCCESS" -> ReservationStatus.CONFIRMED;
            case "FAILED" -> ReservationStatus.FAILED;
            case "EXPIRED" -> ReservationStatus.EXPIRED;
            default -> {
                log.warn("Unknown payment status: '{}', defaulting to CANCELED", paymentStatus);
                yield ReservationStatus.CANCELED;
            }
        };
    }
}