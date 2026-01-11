package com.sep.banksimulator.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorizeCardPaymentResponse {

    private CardPaymentStatus status;

    private FailureReason reason;

    private String globalTransactionId;

    private Instant acquirerTimestamp;

}
