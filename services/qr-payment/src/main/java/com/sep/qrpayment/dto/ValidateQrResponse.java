package com.sep.qrpayment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateQrResponse {

    private boolean valid;
    private String reason;

}
