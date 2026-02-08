package com.sep.psp.dto.payment;

import com.sep.psp.entity.PaymentStatus;
import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String merchantKey;
    private String merchantOrderId;
    private String successUrl;
    private String failedUrl;
    private String errorUrl;

}
