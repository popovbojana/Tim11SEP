package com.sep.webshop.controller;

import com.sep.webshop.dto.CreateReservationRequest;
import com.sep.webshop.dto.ReservationDTO;
import com.sep.webshop.service.RentalReservationService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<ReservationDTO> create(@Valid @RequestBody CreateReservationRequest request, Authentication authentication) {
        return new ResponseEntity<>(reservationService.create(request, authentication.getName()), HttpStatus.CREATED);
    }

    @GetMapping(value = "/history")
    public ResponseEntity<List<ReservationDTO>> getHistory(Authentication authentication) {
        return new ResponseEntity<>(reservationService.getPurchaseHistory(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping(value = "/active")
    public ResponseEntity<List<ReservationDTO>> getActive(Authentication authentication) {
        return new ResponseEntity<>(reservationService.getActiveReservations(authentication.getName()), HttpStatus.OK);
    }

}
