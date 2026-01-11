package com.sep.banksimulator.dto.qr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrRequest {

    private Long bankPaymentId;

    private Long pspPaymentId;

    private Double amount;

    private String currency;

    private String receiverAccount;

    private String receiverName;

    private String purpose;

    private String paymentCode;

    private String referenceNumber;

    private String stan;

    private String pspTimestamp;
}
