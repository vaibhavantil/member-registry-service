package com.hedvig.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class DatadogConfig {
    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry>
    {
        return MeterRegistryCustomizer { registry -> registry.config().commonTags("service", "member-service") }
    }
}
