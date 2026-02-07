package com.sep.webshop.dto.payment;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericCallbackRequest {

    private Long pspPaymentId;
    private String merchantOrderId;
    private String externalPaymentId;
    private String status;
    private String globalTransactionId;
    private String stan;
    private String paymentMethod;
    private Instant pspTimestamp;
    private Instant acquirerTimestamp;

}
