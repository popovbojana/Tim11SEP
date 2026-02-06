package com.sep.webshop.service.impl;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.entity.*;
import com.sep.webshop.exception.BadRequestException;
import com.sep.webshop.exception.NotFoundException;
import com.sep.webshop.repository.RentalOfferRepository;
import com.sep.webshop.repository.RentalReservationRepository;
import com.sep.webshop.service.RentalReservationService;
import com.sep.webshop.service.WebshopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentalReservationServiceImpl implements RentalReservationService {

    private final RentalOfferRepository rentalOfferRepository;
    private final RentalReservationRepository rentalReservationRepository;

    @Override
    @Transactional
    public ReservationDTO create(CreateReservationRequest request, String customerEmail, String merchantOrderId) {
        validateDates(request);

        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(request.getOfferId())
                .orElseThrow(() -> new NotFoundException("Offer with id: " + request.getOfferId() + " not found."));

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days <= 0) {
            throw new BadRequestException("End date must be after start date.");
        }

        double basePerDay = WebshopMapper.basePricePerDay(offer);
        double additionalPerDay = calculateAdditionalPricePerDay(offer, request.getSelectedAdditionalServiceIds());
        double total = (basePerDay + additionalPerDay) * days;

        RentalReservation reservation = RentalReservation.builder()
                .customerEmail(customerEmail)
                .offer(offer)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ReservationStatus.PENDING)
                .totalPrice(total)
                .merchantOrderId(merchantOrderId)
                .build();

        return WebshopMapper.toDTO(rentalReservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO getById(Long id) {
        return WebshopMapper.toDTO(rentalReservationRepository.getReferenceById(id));
    }

    @Override
    @Transactional
    public void updateFromPaymentCallback(String merchantOrderId, Long pspPaymentId, ReservationStatus newStatus,
                                          PaymentMethod method, String reference, Instant paidAt) {

        RentalReservation r = rentalReservationRepository.findByMerchantOrderId(merchantOrderId)
                .orElseThrow(() -> new NotFoundException("Reservation with merchant order id: " + merchantOrderId + " not found."));

        if (pspPaymentId != null) {
            r.setPspPaymentId(pspPaymentId);
        }

        r.setStatus(newStatus);
        r.setPaymentMethod(method);
        r.setPaymentReference(reference);
        r.setPaidAt(paidAt);

        rentalReservationRepository.save(r);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getPurchaseHistory(String customerEmail) {
        return rentalReservationRepository.findAllByCustomerEmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getActiveReservations(String customerEmail) {
        return rentalReservationRepository.findAllByCustomerEmailAndStatusInOrderByCreatedAtDesc(
                        customerEmail,
                        List.of(ReservationStatus.CREATED, ReservationStatus.CONFIRMED)
                ).stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }


    private void validateDates(CreateReservationRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Start and end dates are required.");
        }
    }

    private double calculateAdditionalPricePerDay(RentalOffer offer, Set<Long> selectedAdditionalServiceIds) {
        if (selectedAdditionalServiceIds == null || selectedAdditionalServiceIds.isEmpty()) return 0.0;
        return offer.getAdditionalServices().stream()
                .filter(s -> selectedAdditionalServiceIds.contains(s.getId()))
                .mapToDouble(AdditionalService::getPricePerDay)
                .sum();
    }
}
