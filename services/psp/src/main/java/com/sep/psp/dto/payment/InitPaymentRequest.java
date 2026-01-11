package com.sep.psp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

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
    @Positive
    private Double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String successUrl;

    @NotBlank
    private String failedUrl;

    @NotBlank
    private String errorUrl;
}
