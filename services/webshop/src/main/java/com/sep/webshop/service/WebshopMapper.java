package com.sep.webshop.service;

import com.sep.webshop.dto.*;
import com.sep.webshop.entity.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class WebshopMapper {

    public static VehicleDTO toDTO(Vehicle v) {
        if (v == null) return null;
        return VehicleDTO.builder()
                .id(v.getId())
                .type(v.getType())
                .brand(v.getBrand())
                .model(v.getModel())
                .pricePerDay(v.getPricePerDay())
                .description(v.getDescription())
                .build();
    }

    public static InsurancePackageDTO toDTO(InsurancePackage p) {
        if (p == null) return null;
        return InsurancePackageDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .pricePerDay(p.getPricePerDay())
                .build();
    }

    public static AdditionalServiceDTO toDTO(AdditionalService s) {
        if (s == null) return null;
        return AdditionalServiceDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .pricePerDay(s.getPricePerDay())
                .build();
    }

    public static List<AdditionalServiceDTO> toAdditionalServiceDTOs(Set<AdditionalService> services) {
        if (services == null) return List.of();
        return services.stream().map(WebshopMapper::toDTO).collect(Collectors.toList());
    }

    public static BigDecimal basePricePerDay(RentalOffer offer) {
        if (offer == null || offer.getVehicle() == null || offer.getInsurancePackage() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal vehiclePrice = offer.getVehicle().getPricePerDay();
        BigDecimal insurancePrice = offer.getInsurancePackage().getPricePerDay();

        return vehiclePrice.add(insurancePrice);
    }

    public static RentalOfferDTO toDTO(RentalOffer offer) {
        return RentalOfferDTO.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .vehicle(toDTO(offer.getVehicle()))
                .insurancePackage(toDTO(offer.getInsurancePackage()))
                .additionalServices(toAdditionalServiceDTOs(offer.getAdditionalServices()))
                .basePricePerDay(basePricePerDay(offer))
                .build();
    }

    public static ReservationDTO toDTO(RentalReservation r) {
        RentalOffer offer = r.getOffer();

        return ReservationDTO.builder()
                .id(r.getId())
                .customerEmail(r.getCustomerEmail())
                .offerId(offer.getId())
                .offerTitle(offer.getTitle())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .status(r.getStatus())
                .totalPrice(r.getTotalPrice())
                .currency(r.getCurrency())
                .paymentMethod(r.getPaymentMethod())
                .paymentReference(r.getPaymentReference())
                .paidAt(r.getPaidAt())
                .createdAt(r.getCreatedAt())
                .vehicle(toDTO(offer.getVehicle()))
                .insurancePackage(toDTO(offer.getInsurancePackage()))
                .additionalServices(toAdditionalServiceDTOs(offer.getAdditionalServices()))
                .merchantOrderId(r.getMerchantOrderId())
                .pspPaymentId(r.getPspPaymentId())
                .build();
    }

}
