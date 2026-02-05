package com.sep.psp.dto.merchant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class MerchantCreateRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String merchantKey;

    @NotBlank
    @Size(min = 8)
    private String merchantPassword;

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

    @NotEmpty(message = "At least one payment method must be selected")
    private Set<String> methods;

    @NotBlank
    private String bankAccount;

}