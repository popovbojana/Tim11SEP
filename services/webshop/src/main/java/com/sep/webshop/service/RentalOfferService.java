package com.sep.webshop.service;

import com.sep.webshop.dto.RentalOfferDTO;
import com.sep.webshop.entity.RentalOffer;
import com.sep.webshop.repository.RentalOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalOfferService {

    private final RentalOfferRepository rentalOfferRepository;

    public List<RentalOfferDTO> getActiveOffers() {
        return rentalOfferRepository.findAllByActiveTrue()
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }

    public RentalOfferDTO getOffer(Long offerId) {
        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + offerId));
        return WebshopMapper.toDTO(offer);
    }
}
