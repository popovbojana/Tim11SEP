package com.sep.psp.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericPaymentResponse {

    private String redirectUrl;
    private Long externalPaymentId;

}