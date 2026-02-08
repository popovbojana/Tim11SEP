package com.sep.banksimulator.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteBankPaymentRequest {

    @NotBlank
    private String pan;

    @NotBlank
    private String securityCode;

    @NotBlank
    private String cardHolderName;

    @NotBlank
    private String expiry;
}