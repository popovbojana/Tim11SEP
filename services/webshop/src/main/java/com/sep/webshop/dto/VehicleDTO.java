package com.sep.webshop.dto;

import com.sep.webshop.entity.VehicleType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDTO {

    private Long id;

    private VehicleType type;

    private String brand;

    private String model;

    private double pricePerDay;

    private String description;

}
