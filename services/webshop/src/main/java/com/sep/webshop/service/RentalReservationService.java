package com.sep.webshop.service;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.entity.AdditionalService;
import com.sep.webshop.entity.RentalOffer;
import com.sep.webshop.entity.RentalReservation;
import com.sep.webshop.entity.ReservationStatus;
import com.sep.webshop.repository.RentalOfferRepository;
import com.sep.webshop.repository.RentalReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RentalReservationService {

    private final RentalOfferRepository rentalOfferRepository;

    private final RentalReservationRepository rentalReservationRepository;

    @Transactional
    public ReservationDTO create(CreateReservationRequest request, String customerEmail) {
        validateDates(request);

        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(request.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + request.getOfferId()));

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days <= 0) {
            throw new IllegalArgumentException("End date must be after start date.");
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
                .build();

        return WebshopMapper.toDTO(rentalReservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getPurchaseHistory(String customerEmail) {
        return rentalReservationRepository.findAllByCustomerEmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getActiveReservations(String customerEmail) {
        return rentalReservationRepository.findAllByCustomerEmailAndStatusInOrderByCreatedAtDesc(
                        customerEmail,
                        List.of(ReservationStatus.CREATED, ReservationStatus.CONFIRMED)
                ).stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }

    private static void validateDates(CreateReservationRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new IllegalArgumentException("Start and end date are required.");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }
    }

    private static double calculateAdditionalPricePerDay(RentalOffer offer, Set<Long> selectedIds) {
        if (selectedIds == null || selectedIds.isEmpty()) return 0.0;

        return offer.getAdditionalServices().stream()
                .filter(s -> selectedIds.contains(s.getId()))
                .mapToDouble(AdditionalService::getPricePerDay)
                .sum();
    }
}
