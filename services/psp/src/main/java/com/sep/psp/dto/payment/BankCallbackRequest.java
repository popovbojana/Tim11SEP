package com.sep.psp.dto.payment;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankCallbackRequest {

    private Long pspPaymentId;

    private Long bankPaymentId;

    private String status;

    private String globalTransactionId;

    private Instant acquirerTimestamp;

    private String stan;

    private Instant pspTimestamp;

}
