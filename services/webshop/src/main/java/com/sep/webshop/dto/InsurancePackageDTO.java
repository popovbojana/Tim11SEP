package com.sep.webshop.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePackageDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerDay;

}
