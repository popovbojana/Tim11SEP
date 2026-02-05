package com.sep.banksimulator.dto;

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
    private Long externalPaymentId;
    private String status;
    private String globalTransactionId;
    private String stan;
    private String paymentMethod;
    private Instant pspTimestamp;
    private Instant acquirerTimestamp;

}