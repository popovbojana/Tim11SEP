package com.sep.psp.dto.paymentMethod;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {

    private Long id;

    private String name;

    private String serviceName;

}
