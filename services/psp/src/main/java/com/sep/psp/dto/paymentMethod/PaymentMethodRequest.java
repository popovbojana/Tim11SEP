package com.sep.psp.dto.paymentMethod;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {

    private String name;
    private String serviceName;

}
