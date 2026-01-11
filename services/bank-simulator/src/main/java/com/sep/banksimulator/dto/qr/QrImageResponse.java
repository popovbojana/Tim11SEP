package com.sep.banksimulator.dto.qr;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QrImageResponse {

    private String qrImageBase64;

}
