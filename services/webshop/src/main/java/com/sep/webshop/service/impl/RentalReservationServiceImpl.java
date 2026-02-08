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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalReservationServiceImpl implements RentalReservationService {

    private final RentalOfferRepository rentalOfferRepository;
    private final RentalReservationRepository rentalReservationRepository;

    @Override
    @Transactional
    public ReservationDTO create(CreateReservationRequest request, String customerEmail, String merchantOrderId) {
        log.info("📨 Creating reservation — customer: {}, offer ID: {}, order: {}", customerEmail, request.getOfferId(), merchantOrderId);

        validateDates(request);

        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(request.getOfferId())
                .orElseThrow(() -> {
                    log.warn("❌ Offer NOT FOUND — ID: {}", request.getOfferId());
                    return new NotFoundException("Offer with id: " + request.getOfferId() + " not found.");
                });

        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days <= 0) {
            log.warn("❌ Invalid dates — start: {}, end: {}", request.getStartDate(), request.getEndDate());
            throw new BadRequestException("End date must be after start date.");
        }

        double basePerDay = WebshopMapper.basePricePerDay(offer);
        double additionalPerDay = calculateAdditionalPricePerDay(offer, request.getSelectedAdditionalServiceIds());
        double total = (basePerDay + additionalPerDay) * days;
        log.info("💰 Price calculated — {} days × ({} base + {} additional) = {} total", days, basePerDay, additionalPerDay, total);

        RentalReservation reservation = RentalReservation.builder()
                .customerEmail(customerEmail)
                .offer(offer)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ReservationStatus.PENDING)
                .totalPrice(total)
                .merchantOrderId(merchantOrderId)
                .build();

        RentalReservation saved = rentalReservationRepository.save(reservation);
        log.info("✅ Reservation created — ID: {}, status: PENDING, total: {}", saved.getId(), total);

        return WebshopMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDTO getById(Long id) {
        log.info("🔍 Fetching reservation by ID: {}", id);
        return WebshopMapper.toDTO(rentalReservationRepository.getReferenceById(id));
    }

    @Override
    @Transactional
    public void updateFromPaymentCallback(String merchantOrderId, Long pspPaymentId, ReservationStatus newStatus,
                                          PaymentMethod method, String reference, Instant paidAt) {
        log.info("📨 Payment callback — order: {}, PSP payment ID: {}, status: {}", merchantOrderId, pspPaymentId, newStatus);

        RentalReservation r = rentalReservationRepository.findByMerchantOrderId(merchantOrderId)
                .orElseThrow(() -> {
                    log.error("❌ Reservation NOT FOUND — merchant order ID: {}", merchantOrderId);
                    return new NotFoundException("Reservation with merchant order id: " + merchantOrderId + " not found.");
                });

        if (pspPaymentId != null) {
            r.setPspPaymentId(pspPaymentId);
        }

        r.setStatus(newStatus);
        r.setPaymentMethod(method);
        r.setPaymentReference(reference);
        r.setPaidAt(paidAt);

        rentalReservationRepository.save(r);
        log.info("✅ Reservation updated — ID: {}, new status: {}, method: {}", r.getId(), newStatus, method);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getPurchaseHistory(String customerEmail) {
        log.info("📋 Fetching purchase history for: {}", customerEmail);
        List<ReservationDTO> history = rentalReservationRepository.findAllByCustomerEmailOrderByCreatedAtDesc(customerEmail)
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
        log.info("✅ Found {} reservations for {}", history.size(), customerEmail);
        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDTO> getActiveReservations(String customerEmail) {
        log.info("📋 Fetching active reservations for: {}", customerEmail);
        List<ReservationDTO> active = rentalReservationRepository.findAllByCustomerEmailAndStatusInOrderByCreatedAtDesc(
                        customerEmail,
                        List.of(ReservationStatus.CREATED, ReservationStatus.CONFIRMED)
                ).stream()
                .map(WebshopMapper::toDTO)
                .toList();
        log.info("✅ Found {} active reservations for {}", active.size(), customerEmail);
        return active;
    }

    private void validateDates(CreateReservationRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            log.warn("⚠️ Missing dates — start: {}, end: {}", request.getStartDate(), request.getEndDate());
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