package com.sep.qrpayment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateQrRequest {

    @NotBlank
    private String qrText;

    @NotNull
    private Long bankPaymentId;

    @NotNull
    private Long pspPaymentId;

    @NotNull
    private Double amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String receiverAccount;

    @NotBlank
    private String receiverName;

    private String paymentCode;

    private String purpose;

    private String referenceNumber;

    @NotBlank
    private String stan;
}
