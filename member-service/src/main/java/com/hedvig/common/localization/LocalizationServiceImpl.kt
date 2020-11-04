package com.hedvig.common.localization

import com.hedvig.lokalise.client.LokaliseClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocalizationServiceImplForReal(
    @Value("\${lokalise.useFakes}")
    private val useFakes: Boolean,
    configuration: LokaliseConfigurationProperties
) : LocalizationService {

    val client = if (!useFakes) LokaliseClient(configuration.projectId, configuration.apiToken) else null

    override fun getTranslation(key: String, locale: Locale) =
        if (!useFakes) client!!.getTranslation(key, locale) else "lokalise configuration useFakes is set to true"
}
