package com.hedvig.external.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class JacksonConfiguration {
    @Bean
    open fun objectMapper(): ObjectMapper =
        ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
}
