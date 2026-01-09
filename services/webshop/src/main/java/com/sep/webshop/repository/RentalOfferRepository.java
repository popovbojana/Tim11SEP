package com.sep.webshop.repository;

import com.sep.webshop.entity.RentalOffer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalOfferRepository extends JpaRepository<RentalOffer, Long> {

    @EntityGraph(attributePaths = {"vehicle", "insurancePackage", "additionalServices"})
    List<RentalOffer> findAllByActiveTrue();

    @EntityGraph(attributePaths = {"vehicle", "insurancePackage", "additionalServices"})
    Optional<RentalOffer> findByIdAndActiveTrue(Long id);

}
