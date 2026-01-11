package com.sep.webshop.dto.payment;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebshopPaymentCallbackRequest {

    private String merchantOrderId;

    private Long pspPaymentId;

    private String status;

    private String paymentReference;

    private Instant paidAt;

    private String paymentMethod;
}
