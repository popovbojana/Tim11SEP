package com.sep.webshop.service.impl;

import com.sep.webshop.dto.RentalOfferDTO;
import com.sep.webshop.entity.RentalOffer;
import com.sep.webshop.exception.NotFoundException;
import com.sep.webshop.repository.RentalOfferRepository;
import com.sep.webshop.service.RentalOfferService;
import com.sep.webshop.service.WebshopMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalOfferServiceImpl implements RentalOfferService {

    private final RentalOfferRepository rentalOfferRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RentalOfferDTO> getActiveOffers() {
        log.info("Fetching all active rental offers");
        List<RentalOfferDTO> offers = rentalOfferRepository.findAllByActiveTrue()
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
        log.info("Found {} active offers", offers.size());
        return offers;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalOfferDTO getOffer(Long offerId) {
        log.info("Fetching rental offer by ID: {}", offerId);
        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(offerId)
                .orElseThrow(() -> {
                    log.warn("Offer NOT FOUND — ID: {}", offerId);
                    return new NotFoundException("Offer with id: " + offerId + " not found.");
                });
        log.info("Offer found — ID: {}", offerId);
        return WebshopMapper.toDTO(offer);
    }
}