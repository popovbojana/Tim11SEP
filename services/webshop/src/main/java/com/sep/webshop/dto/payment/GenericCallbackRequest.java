package com.sep.webshop.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericCallbackRequest {

    @NotNull(message = "PSP Payment id is required.")
    private Long pspPaymentId;

    @NotBlank(message = "Merchant order id is required.")
    private String merchantOrderId;

    @NotNull(message = "Amount is required.")
    private BigDecimal amount;

    private String externalPaymentId;

    @NotBlank(message = "Status is required.")
    private String status;

    private String globalTransactionId;

    private String stan;

    @NotBlank(message = "Payment method is required.")
    private String paymentMethod;

    private Instant pspTimestamp;

    private Instant acquirerTimestamp;

}
