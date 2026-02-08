package com.sep.psp.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitPaymentResponse {

    private Long paymentId;
    private String redirectUrl;

}
