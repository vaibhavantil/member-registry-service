package com.hedvig.common.localization

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "lokalise")
//@ConditionalOnProperty(value = ["localizedCashback"], matchIfMissing = false)
class LokaliseConfigurationProperties {
    lateinit var projectId: String
    lateinit var apiToken: String
}
