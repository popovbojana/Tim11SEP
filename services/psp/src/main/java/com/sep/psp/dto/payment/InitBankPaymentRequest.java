package com.sep.psp.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitBankPaymentRequest {

    @NotNull
    private Long pspPaymentId;

    @Positive
    private double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String stan;

    @NotNull
    private Instant pspTimestamp;
}
