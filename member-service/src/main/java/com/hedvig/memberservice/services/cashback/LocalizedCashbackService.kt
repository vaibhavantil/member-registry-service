package com.hedvig.memberservice.services.cashback

import com.hedvig.common.localization.LocalizationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.memberservice.web.dto.NonLocalizedCashbackOption
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.*

interface CashbackService {
    fun getCashbackOption(cashbackId: UUID?, pickedLocale: PickedLocale?): Optional<CashbackOption>
    fun getOptions(pickedLocale: PickedLocale?): List<CashbackOption>
    fun getDefaultId(pickedLocale: PickedLocale?): UUID
    fun getDefaultCashback(pickedLocale: PickedLocale?): CashbackOption?
}

@Component
@ConditionalOnProperty(name = ["localizedCashback"], havingValue = "true")
class LocalizedCashbackService(
    private val localizationService: LocalizationService
) : CashbackService {

    override fun getCashbackOption(cashbackId: UUID?, pickedLocale: PickedLocale?): Optional<CashbackOption> {
        val locale = pickedLocale?.locale ?: DEFAULT_LOCALE.locale
        val cashbackOption = norwegianOptions[cashbackId]
        if (cashbackOption != null) {
            return Optional.ofNullable(localize(cashbackOption, locale))
        }
        return Optional.ofNullable(swedishOptions[cashbackId]?.let { localize(it, locale) })
    }

    override fun getOptions(pickedLocale: PickedLocale?) = when (pickedLocale ?: DEFAULT_LOCALE) {
        PickedLocale.sv_SE,
        PickedLocale.en_SE -> swedishOptions
        PickedLocale.nb_NO,
        PickedLocale.en_NO -> norwegianOptions
        PickedLocale.da_DK,
        PickedLocale.en_DK -> danishOptions
    }.values.toList().map{ localize(it, pickedLocale?.locale ?: DEFAULT_LOCALE.locale) }

    override fun getDefaultId(pickedLocale: PickedLocale?): UUID = when (pickedLocale ?: DEFAULT_LOCALE) {
        PickedLocale.sv_SE,
        PickedLocale.en_SE -> UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2")
        PickedLocale.nb_NO,
        PickedLocale.en_NO -> UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003")
        PickedLocale.da_DK,
        PickedLocale.en_DK -> UUID.fromString("00000000-0000-0000-0000-000000000000")
    }

    override fun getDefaultCashback(pickedLocale: PickedLocale?) = when (pickedLocale ?: DEFAULT_LOCALE) {
        PickedLocale.sv_SE,
        PickedLocale.en_SE -> swedishOptions[getDefaultId(pickedLocale)]
        PickedLocale.nb_NO,
        PickedLocale.en_NO -> norwegianOptions[getDefaultId(pickedLocale)]
        PickedLocale.da_DK,
        PickedLocale.en_DK -> danishOptions[getDefaultId(pickedLocale)]
    }?.let { localize(it, pickedLocale?.locale ?: DEFAULT_LOCALE.locale) }

    private fun localize(nonLocalized: NonLocalizedCashbackOption, locale: Locale) = CashbackOption(
        id = nonLocalized.id,
        name = localizationService.getTranslation(nonLocalized.nameKey, locale),
        title = localizationService.getTranslation(nonLocalized.titleKey, locale),
        description = localizationService.getTranslation(nonLocalized.descriptionKey,locale),
        selected = nonLocalized.selected,
        charity = nonLocalized.charity,
        imageUrl = nonLocalized.imageUrl,
        selectedUrl = nonLocalized.selectedUrl,
        signature = localizationService.getTranslation(nonLocalized.signatureKey, locale),
        paragraph = localizationService.getTranslation(nonLocalized.paragraphKey, locale)
    )

    private val option3 = NonLocalizedCashbackOption(
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
        "CASHBACK_SOSBARNBYAR_TITLE",
        "CASHBACK_SOSBARNBYAR_TITLE",
        "CASHBACK_SOSBARNBYAR_DESCRIPTION",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "",
        "CASHBACK_SOSBARNBYAR_DESCRIPTION"
    )
    private val option2 = NonLocalizedCashbackOption(
        UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
        "CASHBACK_BARNCANCERFONDEN_TITLE",
        "CASHBACK_BARNCANCERFONDEN_TITLE",
        "CASHBACK_BARNCANCERFONDEN_DESCRIPTION",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "",
        "CASHBACK_BARNCANCERFONDEN_DESCRIPTION"
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

    private val danishOptions = mapOf<UUID, NonLocalizedCashbackOption>()

    private val DEFAULT_LOCALE = PickedLocale.sv_SE
}
