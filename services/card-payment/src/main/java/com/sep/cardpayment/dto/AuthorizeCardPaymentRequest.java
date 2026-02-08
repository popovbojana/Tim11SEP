package com.sep.cardpayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeCardPaymentRequest {

    @NotNull
    private Long bankPaymentId;

    @Positive
    private double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String pan;

    @NotBlank
    private String securityCode;

    @NotBlank
    private String cardHolderName;

    @NotBlank
    private String expiry;
}
