package com.sep.webshop.dto.payment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitPaymentRequest {

    private String merchantKey;
    private String merchantOrderId;
    private Long reservationId;
    private BigDecimal amount;
    private String currency;

}
