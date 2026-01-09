package com.sep.webshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rental_offers")
public class RentalOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insurance_package_id")
    private InsurancePackage insurancePackage;

    @ManyToMany
    @JoinTable(
            name = "rental_offer_additional_services",
            joinColumns = @JoinColumn(name = "offer_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @Builder.Default
    private Set<AdditionalService> additionalServices = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;
}
