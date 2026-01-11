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

    private String stan;

    private String pspTimestamp;

}
