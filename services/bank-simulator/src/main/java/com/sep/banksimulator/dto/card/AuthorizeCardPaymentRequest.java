package com.sep.banksimulator.dto.card;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeCardPaymentRequest {

    private Long bankPaymentId;
    private BigDecimal amount;
    private String currency;
    private String pan;
    private String securityCode;
    private String cardHolderName;
    private String expiry;

}