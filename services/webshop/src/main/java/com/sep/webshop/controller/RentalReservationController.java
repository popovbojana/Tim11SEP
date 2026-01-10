package com.sep.webshop.controller;

import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.service.RentalReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class RentalReservationController {

    private final RentalReservationService reservationService;

    @GetMapping(value = "/history")
    public ResponseEntity<List<ReservationDTO>> getHistory(Authentication authentication) {
        return new ResponseEntity<>(reservationService.getPurchaseHistory(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping(value = "/active")
    public ResponseEntity<List<ReservationDTO>> getActive(Authentication authentication) {
        return new ResponseEntity<>(reservationService.getActiveReservations(authentication.getName()), HttpStatus.OK);
    }

}
