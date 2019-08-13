package com.hedvig.config

import com.fasterxml.jackson.databind.Module
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.jackson.datatype.money.MoneyModule

@Configuration
class JacksonConfig {
    @Bean
    fun monetaModule(): Module = MoneyModule().withQuotedDecimalNumbers()
}
