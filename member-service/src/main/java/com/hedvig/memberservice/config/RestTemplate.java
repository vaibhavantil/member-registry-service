package com.hedvig.memberservice.config;

import com.hedvig.memberservice.CustomClientHttpRequestInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplate {

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate(
            RestTemplateBuilder builder,
            CustomClientHttpRequestInterceptor customClientHttpRequestInterceptor) {
        org.springframework.web.client.RestTemplate restTemplate =
                new org.springframework.web.client.RestTemplate(
                        new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        List<ClientHttpRequestInterceptor> interceptors =
                Collections.singletonList(customClientHttpRequestInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

}
