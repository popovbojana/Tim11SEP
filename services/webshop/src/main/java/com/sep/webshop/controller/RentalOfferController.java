package com.sep.webshop.controller;

import com.sep.webshop.dto.RentalOfferDTO;
import com.sep.webshop.service.RentalOfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/offers")
public class RentalOfferController {

    private final RentalOfferService offerService;

    @GetMapping
    public ResponseEntity<List<RentalOfferDTO>> getActiveOffers() {
        return new ResponseEntity<>(offerService.getActiveOffers(), HttpStatus.OK);
    }

    @GetMapping(value = "/{offerId}")
    public ResponseEntity<RentalOfferDTO> getOfferDetails(@PathVariable Long offerId) {
        return new ResponseEntity<>(offerService.getOffer(offerId), HttpStatus.OK);
    }

}
