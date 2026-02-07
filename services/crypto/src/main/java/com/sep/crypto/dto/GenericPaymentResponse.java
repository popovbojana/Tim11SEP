package com.sep.crypto.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericPaymentResponse {

    private String redirectUrl;
    private String externalPaymentId;

}