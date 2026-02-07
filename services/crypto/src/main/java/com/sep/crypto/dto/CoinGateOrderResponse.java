package com.sep.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinGateOrderResponse {

    private Long id;

    private String status;

    @JsonProperty("payment_url")
    private String paymentUrl;

    private String token;

}