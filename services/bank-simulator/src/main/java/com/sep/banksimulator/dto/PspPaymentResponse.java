package com.sep.banksimulator.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PspPaymentResponse {

    private Long id;

    private String successUrl;

    private String failedUrl;

    private String errorUrl;

}
