package com.hedvig.memberservice.services.cashback

import com.hedvig.common.localization.LocalizationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.memberservice.web.dto.NonLocalizedCashbackOption
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

interface CashbackService {
    fun getCashbackOption(cashbackId: UUID?, pickedLocale: PickedLocale): Optional<CashbackOption>
    fun getOptions(pickedLocale: PickedLocale): List<CashbackOption>
    fun getDefaultId(pickedLocale: PickedLocale): UUID
    fun getDefaultCashback(pickedLocale: PickedLocale): CashbackOption?
}

@Component
@ConditionalOnProperty(name = ["localizedCashback"], havingValue = "true")
class LocalizedCashbackService(
    private val localizationService: LocalizationService
) : CashbackService {

    init {
        println()
    }

    override fun getCashbackOption(cashbackId: UUID?, pickedLocale: PickedLocale): Optional<CashbackOption> {
        try {
            val cashbackOption = norwegianOptions[cashbackId]
            if (cashbackOption != null) return Optional.ofNullable(localize(cashbackOption, pickedLocale.locale))
        } catch (ex: NullPointerException) {
        }
        return Optional.ofNullable(swedishOptions[cashbackId]?.let { localize(it, pickedLocale.locale) })
    }

    override fun getOptions(pickedLocale: PickedLocale) =
        if (isNorwegian(pickedLocale)) {
            norwegianOptions.values.toList().map { localize(it, pickedLocale.locale) }
        } else swedishOptions.values.toList().map { localize(it, pickedLocale.locale) }

    override fun getDefaultId(pickedLocale: PickedLocale): UUID = if (isNorwegian(pickedLocale)) {
        UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003")
    } else {
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2")
    }

    override fun getDefaultCashback(pickedLocale: PickedLocale) =
        swedishOptions[getDefaultId(pickedLocale)]?.let { localize(it, pickedLocale.locale) }

    private fun localize(nonLocalized: NonLocalizedCashbackOption, locale: Locale) = CashbackOption(
        nonLocalized.id,
        localizationService.getTranslation(nonLocalized.nameKey, locale),
        localizationService.getTranslation(nonLocalized.titleKey, locale),
        localizationService.getTranslation(nonLocalized.descriptionKey,locale),
        nonLocalized.selected,
        nonLocalized.charity,
        nonLocalized.imageUrl,
        nonLocalized.selectedUrl,
        localizationService.getTranslation(nonLocalized.signatureKey, locale),
        localizationService.getTranslation(nonLocalized.paragraphKey, locale)
    )

    private fun isNorwegian(pickedLocale: PickedLocale): Boolean {
        return pickedLocale === PickedLocale.nb_NO || pickedLocale === PickedLocale.en_NO
    }

    private val option3 = NonLocalizedCashbackOption(
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
        "CASHBACK_SOSBARNBYAR_NAME",
        "CASHBACK_SOSBARNBYAR_NAME",
        "CASHBACK_SOSBARNBYAR_DESCRIPTION",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "",
        "CASHBACK_SOSBARNBYAR_PARAGRAPH"
    )
    private val option2 = NonLocalizedCashbackOption(
        UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
        "CASHBACK_BARNCANCERFONDEN_NAME",
        "CASHBACK_BARNCANCERFONDEN_TITLE",
        "CASHBACK_BARNCANCERFONDEN_DESCRIPTION",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "",
        "CASHBACK_BARNCANCERFONDEN_PARAGRAPH"
    )
    private val norwayOption = NonLocalizedCashbackOption(
        UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003"),
        "CASHBACK_SOSBARNBYAR_NAME",
        "CASHBACK_SOSBARNBYAR_NAME",
        "CASHBACK_SOSBARNBYAR_DESCRIPTION",
        false,
        true,
        "https://com-hedvig-web-content.s3.eu-central-1.amazonaws.com/Til+inntekt+for+SOS+Barnebyer.png",
        "https://com-hedvig-web-content.s3.eu-central-1.amazonaws.com/Til+inntekt+for+SOS+Barnebyer.png",
        "",
        "CASHBACK_SOSBARNBYAR_PARAGRAPH"
    )
    private val swedishOptions = mapOf(
        option3.id to option3,
        option2.id to option2
    )
    private val norwegianOptions = mapOf(
        norwayOption.id to norwayOption
    )
}
