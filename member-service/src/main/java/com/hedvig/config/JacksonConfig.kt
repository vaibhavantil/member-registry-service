package com.hedvig.config

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.jackson.datatype.money.MoneyModule

@Configuration
class JacksonConfig {
    @Bean
    fun monetaModule(): Module = MoneyModule().withQuotedDecimalNumbers()
}
