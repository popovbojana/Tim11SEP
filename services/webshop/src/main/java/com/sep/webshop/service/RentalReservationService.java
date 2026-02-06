package com.sep.webshop.service;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.entity.PaymentMethod;
import com.sep.webshop.entity.ReservationStatus;

import java.time.Instant;
import java.util.List;

public interface RentalReservationService {

    ReservationDTO create(CreateReservationRequest request, String customerEmail, String merchantOrderId);
    ReservationDTO getById(Long id);
    void updateFromPaymentCallback(String merchantOrderId, Long pspPaymentId, ReservationStatus newStatus,
                                   PaymentMethod method, String reference, Instant paidAt);
    List<ReservationDTO> getPurchaseHistory(String customerEmail);
    List<ReservationDTO> getActiveReservations(String customerEmail);

}
