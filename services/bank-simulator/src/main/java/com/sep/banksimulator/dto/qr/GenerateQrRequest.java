package com.sep.banksimulator.dto.qr;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrRequest {

    private Long bankPaymentId;
    private Long pspPaymentId;
    private BigDecimal amount;
    private String currency;
    private String receiverAccount;
    private String receiverName;
    private String purpose;
    private String paymentCode;
    private String referenceNumber;
    private String stan;
    private String pspTimestamp;

}
