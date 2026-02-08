package com.sep.cardpayment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckBalanceRequest {

    private String pan;

    private String cvv;

    private Double amount;
}