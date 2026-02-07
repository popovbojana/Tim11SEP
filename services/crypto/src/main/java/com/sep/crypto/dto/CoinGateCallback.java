package com.sep.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinGateCallback {

    private Integer id;

    @JsonProperty("order_id")
    private String orderId;

    private String status;

    @JsonProperty("price_amount")
    private Double priceAmount;

    @JsonProperty("price_currency")
    private String priceCurrency;

    @JsonProperty("receive_amount")
    private String receiveAmount;

    @JsonProperty("receive_currency")
    private String receiveCurrency;

    @JsonProperty("created_at")
    private String createdAt;

}