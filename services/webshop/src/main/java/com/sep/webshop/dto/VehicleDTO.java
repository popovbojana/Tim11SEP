package com.sep.webshop.dto;

import com.sep.webshop.entity.VehicleType;
import lombok.*;

import java.math.BigDecimal;

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
    private BigDecimal pricePerDay;
    private String description;

}
