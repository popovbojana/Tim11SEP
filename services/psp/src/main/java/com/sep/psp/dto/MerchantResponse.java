package com.sep.psp.dto;

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

    private Set<String> activeMethods;

}
