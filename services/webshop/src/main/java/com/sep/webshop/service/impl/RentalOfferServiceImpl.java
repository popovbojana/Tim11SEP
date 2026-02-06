package com.sep.webshop.service.impl;

import com.sep.webshop.dto.RentalOfferDTO;
import com.sep.webshop.entity.RentalOffer;
import com.sep.webshop.exception.NotFoundException;
import com.sep.webshop.repository.RentalOfferRepository;
import com.sep.webshop.service.RentalOfferService;
import com.sep.webshop.service.WebshopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalOfferServiceImpl implements RentalOfferService {

    private final RentalOfferRepository rentalOfferRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RentalOfferDTO> getActiveOffers() {
        return rentalOfferRepository.findAllByActiveTrue()
                .stream()
                .map(WebshopMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RentalOfferDTO getOffer(Long offerId) {
        RentalOffer offer = rentalOfferRepository.findByIdAndActiveTrue(offerId)
                .orElseThrow(() -> new NotFoundException("Offer with id: " + offerId + " not found."));
        return WebshopMapper.toDTO(offer);

    }
}
