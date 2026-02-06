package com.sep.webshop.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitPaymentRequest {

    private String merchantKey;
    private String merchantOrderId;
    private double amount;
    private String currency;
    private String successUrl;
    private String failedUrl;
    private String errorUrl;

}
