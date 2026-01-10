package com.sep.psp.dto.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartPaymentResponse {

    private String redirectUrl;

}
