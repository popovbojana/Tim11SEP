package com.sep.webshop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalServiceDTO {

    private Long id;
    private String name;
    private String description;
    private double pricePerDay;

}
