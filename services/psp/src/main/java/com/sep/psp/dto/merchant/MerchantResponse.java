package com.sep.psp.dto.merchant;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantResponse {

    private Long id;
    private String merchantKey;
    private String fullName;
    private String email;
    private String successUrl;
    private String failedUrl;
    private String errorUrl;
    private String webhookUrl;
    private Set<String> activeMethods;

}