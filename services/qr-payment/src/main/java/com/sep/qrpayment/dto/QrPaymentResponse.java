package com.sep.qrpayment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrPaymentResponse {

    private String qrText;

    private String qrImageBase64;

}
