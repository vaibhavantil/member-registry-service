package com.hedvig.external.config

import feign.Request
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


open class ZignSecFeignConfig {

    @Bean
    open fun opts(
        @Value("\${zign.sec.feign.connectTimeoutMillis:1000}") connectTimeoutMillis: Int,
        @Value("\${zign.sec.feign.readTimeoutMillis:30000}") readTimeoutMillis: Int): Request.Options? {
        return Request.Options(connectTimeoutMillis, readTimeoutMillis)
    }
}
