package com.hedvig.memberservice.external.trustpilot

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

class TrustpilotFeignConfig {

    @Bean
    fun oauth2RequestInterceptor(
        template: RestTemplate,
        @Value("\${trustpilot.oauth.apikey}") apikey: String,
        @Value("\${trustpilot.oauth.secret}") secret: String,
        @Value("\${trustpilot.oauth.username}") username: String,
        @Value("\${trustpilot.oauth.password}") password: String
    ): RequestInterceptor {
        return TrustpilotOauth2Interceptor(template, apikey, secret, username, password)
    }
}
