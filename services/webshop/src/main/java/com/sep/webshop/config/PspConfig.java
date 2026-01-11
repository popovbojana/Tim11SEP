package com.sep.webshop.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PspConfig {

    @Value("${psp.base-url}")
    private String baseUrl;

    @Value("${psp.merchant-key}")
    private String merchantKey;

}
