package com.sep.psp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantCreateRequest {

    @NotBlank
    private String merchantKey;

}
