package com.sep.banksimulator.dto.qr;

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
