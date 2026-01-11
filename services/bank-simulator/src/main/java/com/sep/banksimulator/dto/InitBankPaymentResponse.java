package com.sep.banksimulator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitBankPaymentResponse {

    private Long bankPaymentId;

    private String redirectUrl;

}
