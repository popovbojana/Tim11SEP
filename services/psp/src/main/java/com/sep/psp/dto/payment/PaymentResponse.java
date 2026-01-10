package com.sep.psp.dto.payment;

import com.sep.psp.entity.PaymentStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;

    private double amount;

    private PaymentStatus status;

    private String merchantKey;

    private String merchantOrderId;

}
