package com.hedvig.memberservice.external.trustpilot

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

class TrustpilotFeignConfig {

    @Bean
    fun oauth2RequestInterceptor(template: RestTemplate, config: TrustpilotOauth2Configuration): RequestInterceptor {
        return TrustpilotOauth2Interceptor(template, config)
    }
}

