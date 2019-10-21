package com.hedvig.integration.customerIO

import feign.auth.BasicAuthRequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class CustomerIOFeignConfiguration (
    @Value("\${customerio.username}") val username: String,
    @Value("\${customerio.password}") val password: String
){
    @Bean
    fun basicAuthRequestInterceptor() = BasicAuthRequestInterceptor(username, password)
}
