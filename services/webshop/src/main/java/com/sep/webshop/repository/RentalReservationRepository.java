package com.sep.webshop.repository;

import com.sep.webshop.entity.RentalReservation;
import com.sep.webshop.entity.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalReservationRepository extends JpaRepository<RentalReservation, Long> {

    @EntityGraph(attributePaths = {"offer", "offer.vehicle", "offer.insurancePackage", "offer.additionalServices"})
    List<RentalReservation> findAllByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    @EntityGraph(attributePaths = {"offer", "offer.vehicle", "offer.insurancePackage", "offer.additionalServices"})
    List<RentalReservation> findAllByCustomerEmailAndStatusInOrderByCreatedAtDesc(String customerEmail, List<ReservationStatus> statuses);

}
