package com.sep.banksimulator.dto.qr;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmQrPaymentRequest {

    @NotBlank
    private String qrText;

}
