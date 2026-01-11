package com.sep.webshop.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalOfferDTO {

    private Long id;

    private String title;

    private String description;

    private VehicleDTO vehicle;

    private InsurancePackageDTO insurancePackage;

    private List<AdditionalServiceDTO> additionalServices;

    private double basePricePerDay;
}
