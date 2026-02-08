package com.sep.psp.dto.paymentMethod;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String serviceName;

}
