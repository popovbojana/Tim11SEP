package com.sep.banksimulator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setRequestFactory(new SimpleClientHttpRequestFactory() {{
            setConnectTimeout(5000);
            setReadTimeout(5000);
        }});

        return restTemplate;
    }
}
