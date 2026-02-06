package com.sep.psp.dto.payment;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericPaymentRequest {

    private Long pspPaymentId;
    private Double amount;
    private String currency;
    private String stan;
    private Instant pspTimestamp;
    private Map<String, String> metadata;

}