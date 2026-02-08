package com.sep.webshop.service;

import com.sep.webshop.dto.RentalOfferDTO;

import java.util.List;

public interface RentalOfferService {

    List<RentalOfferDTO> getActiveOffers();
    RentalOfferDTO getOffer(Long offerId);

}
