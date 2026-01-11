package com.sep.qrpayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrRequest {

    @NotNull
    private Long bankPaymentId;

    @NotNull
    private Long pspPaymentId;

    @NotNull
    private Double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String stan;

    @NotBlank
    private String pspTimestamp;

}
