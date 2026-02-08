package com.sep.psp.dto.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class MerchantUpdateRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String successUrl;

    @NotBlank
    private String failedUrl;

    @NotBlank
    private String errorUrl;

    @NotBlank
    private String serviceName;

    @NotBlank
    private String bankAccount;

    @NotEmpty(message = "At least one payment method must be selected")
    private Set<String> methods;
}