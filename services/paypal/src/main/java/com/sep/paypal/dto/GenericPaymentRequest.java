package com.sep.paypal.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericPaymentRequest {

    private Long pspPaymentId;
    private BigDecimal amount;
    private String currency;
    private String stan;
    private Instant pspTimestamp;
    private Map<String, String> metadata;

}