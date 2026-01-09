package com.sep.webshop.dto;

import com.sep.webshop.entity.PaymentMethod;
import com.sep.webshop.entity.ReservationStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDTO {

    private Long id;

    private String customerEmail;

    private Long offerId;

    private String offerTitle;

    private LocalDate startDate;

    private LocalDate endDate;

    private ReservationStatus status;

    private double totalPrice;

    private PaymentMethod paymentMethod;

    private String paymentReference;

    private Instant paidAt;

    private Instant createdAt;

    private VehicleDTO vehicle;

    private InsurancePackageDTO insurancePackage;

    private List<AdditionalServiceDTO> additionalServices;

}
