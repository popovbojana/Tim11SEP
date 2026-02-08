package com.sep.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoinGateOrderRequest {

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("price_amount")
    private BigDecimal priceAmount;

    @JsonProperty("price_currency")
    private String priceCurrency;

    @JsonProperty("receive_currency")
    private String receiveCurrency;

    @JsonProperty("title")
    private String title;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("success_url")
    private String successUrl;

    @JsonProperty("cancel_url")
    private String cancelUrl;

}