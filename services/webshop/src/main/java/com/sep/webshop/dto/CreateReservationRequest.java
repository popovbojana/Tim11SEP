package com.sep.webshop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReservationRequest {

    @NotNull
    private Long offerId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private Set<Long> selectedAdditionalServiceIds;

}
