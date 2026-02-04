package com.sep.psp.dto.merchant;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MerchantMethodsRequest {

    @NotEmpty
    private Set<String> methods;

}
