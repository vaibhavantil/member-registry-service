package com.hedvig.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate(
            RestTemplateBuilder builder) {
      return new org.springframework.web.client.RestTemplate(
              new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }

}
