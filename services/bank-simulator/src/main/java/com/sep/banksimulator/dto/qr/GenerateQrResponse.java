package com.sep.banksimulator.dto.qr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateQrResponse {

    private String qrText;
    private String qrImageBase64;

}
