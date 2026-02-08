package com.sep.psp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitPaymentRequest {

    @NotBlank
    private String merchantKey;

    @NotBlank
    private String merchantOrderId;

    @NotNull
    private Long reservationId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

}
