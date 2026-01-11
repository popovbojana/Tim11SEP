package com.sep.banksimulator.dto;

import com.sep.banksimulator.entity.BankPaymentStatus;
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

    private BankPaymentStatus status;

    private String globalTransactionId;

    private Instant acquirerTimestamp;

    private String stan;

    private Instant pspTimestamp;

}
