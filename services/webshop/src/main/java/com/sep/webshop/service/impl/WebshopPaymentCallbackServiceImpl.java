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
        if (request == null || request.getMerchantOrderId() == null || request.getMerchantOrderId().isBlank()) {
            log.error("Received malformed payment callback: {}", request);
            throw new BadRequestException("Missing merchant order id.");
        }

        log.info("Processing callback for Order: {} | Status: {} | Method: {}",
                request.getMerchantOrderId(), request.getStatus(), request.getPaymentMethod());

        reservationService.updateFromPaymentCallback(
                request.getMerchantOrderId(),
                request.getPspPaymentId(),
                mapReservationStatus(request.getStatus()),
                request.getPaymentMethod(),
                request.getGlobalTransactionId(),
                request.getAcquirerTimestamp() != null ? request.getAcquirerTimestamp() : Instant.now()
        );

        log.info("Order {} successfully updated to status {}",
                request.getMerchantOrderId(), mapReservationStatus(request.getStatus()));
    }

    private ReservationStatus mapReservationStatus(String paymentStatus) {
        if (paymentStatus == null) return ReservationStatus.FAILED;

        return switch (paymentStatus.toUpperCase()) {
            case "SUCCESS" -> ReservationStatus.CONFIRMED;
            case "FAILED" -> ReservationStatus.FAILED;
            case "EXPIRED" -> ReservationStatus.EXPIRED;
            default -> {
                log.warn("Unknown payment status received: {}. Defaulting to CANCELED", paymentStatus);
                yield ReservationStatus.CANCELED;
            }
        };
    }
}