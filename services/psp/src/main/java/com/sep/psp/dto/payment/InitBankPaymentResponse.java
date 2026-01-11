package com.sep.psp.dto.payment;

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
