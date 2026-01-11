package com.sep.banksimulator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeCardPaymentRequest {

    private Long bankPaymentId;

    private double amount;

    private String currency;

    private String pan;

    private String securityCode;

    private String cardHolderName;

    private String expiry; // "MM/YY"
}
