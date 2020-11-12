package com.hedvig.common.localization

import com.hedvig.lokalise.repository.LokaliseRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class LocalizationServiceImpl(
    @Value("\${lokalise.useFakes}")
    private val useFakes: Boolean,
    configuration: LokaliseConfigurationProperties
) : LocalizationService {

    val repository = if (!useFakes) LokaliseRepository(configuration.projectId, configuration.apiToken) else null

    override fun getTranslation(key: String, locale: Locale, replacements: Map<String, String>) =
        if (!useFakes) repository!!.getTranslation(
            key,
            locale,
            replacements
        ) else "lokalise configuration useFakes is set to true"
}
