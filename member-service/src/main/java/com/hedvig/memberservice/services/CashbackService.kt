package com.hedvig.memberservice.services

import com.hedvig.localization.service.LocalizationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.web.dto.CashbackOption
import com.hedvig.memberservice.web.dto.NonLocalizedCashbackOption
import org.springframework.stereotype.Component
import java.util.*

@Component
class CashbackService(
    private val localizationService: LocalizationService
) {
    fun getCashbackOption(cashbackId: UUID?, pickedLocale: PickedLocale): Optional<CashbackOption> {
        try {
            val cashbackOption = norwegianOptions[cashbackId]
            if (cashbackOption != null) return Optional.ofNullable(localize(cashbackOption, pickedLocale.locale))
        } catch (ex: NullPointerException) {
        }
        return Optional.ofNullable(swedishOptions[cashbackId]?.let { localize(it, pickedLocale.locale) })
    }

    fun getOptions(pickedLocale: PickedLocale) =
        if (isNorwegian(pickedLocale)) {
            norwegianOptions.values.toList().map { localize(it, pickedLocale.locale) }
        } else swedishOptions.values.toList().map { localize(it, pickedLocale.locale) }

    fun getDefaultId(pickedLocale: PickedLocale): UUID = if (isNorwegian(pickedLocale)) {
        UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003")
    } else {
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2")
    }

    fun getDefaultCashback(pickedLocale: PickedLocale) =
        swedishOptions[getDefaultId(pickedLocale)]?.let { localize(it, pickedLocale.locale) }

    private fun localize(nonLocalized: NonLocalizedCashbackOption, locale: Locale) = CashbackOption(
        nonLocalized.id,
        localizationService.getText(locale, nonLocalized.nameKey),
        localizationService.getText(locale, nonLocalized.titleKey),
        localizationService.getText(locale, nonLocalized.descriptionKey),
        nonLocalized.selected,
        nonLocalized.charity,
        nonLocalized.imageUrl,
        nonLocalized.selectedUrl,
        localizationService.getText(locale, nonLocalized.signatureKey),
        localizationService.getText(locale, nonLocalized.paragraphKey)
    )

    private fun isNorwegian(pickedLocale: PickedLocale): Boolean {
        return pickedLocale === PickedLocale.nb_NO || pickedLocale === PickedLocale.en_NO
    }

    private val option3 = NonLocalizedCashbackOption(
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
        "SOS Barnbyar",
        "Ge fler barn en trygg uppväxt",
        "SOS Barnbyar gör ett fantastiskt arbete för att hjälpa barn som förlorat allt. Hos SOS Barnbyar får barnen en familj och en uppväxt i ett tryggt hem och möjlighet att gå i skolan med ambitionen att ta sig ur fattigdomen.",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "",
        "När Hedvig har betalat årets alla skador går din andel av överskottet till att ge fler barn en trygg uppväxt"
    )
    private val option2 = NonLocalizedCashbackOption(
        UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
        "Barncancerfonden",
        "Var med i kampen mot barncancer",
        "Barncancerfonden arbetar för att bekämpa barncancer och se till att drabbade och deras familjer får den vård och stöd de behöver. Pengarna går till forskning och stöd till de cirka 300 familjer som varje år drabbas av ett cancerbesked.",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "",
        "När Hedvig har betalat årets alla skador går din andel av överskottet till att stödja kampen mot barncancer"
    )
    private val norwayOption = NonLocalizedCashbackOption(
        UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003"),
        "SOS Barnbyar",
        "Gi flere barn en trygg oppvekst",
        "SOS-barnebyer ser en fantastisk jobb med å hjelpe barn som har mistet alt. I SOS-barnebyer får barna en familie og oppvekst i et trygt hjem og muligheten til å gå på skole med ambisjoner om å komme seg ut av fattigdom.",
        false,
        true,
        "https://com-hedvig-web-content.s3.eu-central-1.amazonaws.com/Til+inntekt+for+SOS+Barnebyer.png",
        "https://com-hedvig-web-content.s3.eu-central-1.amazonaws.com/Til+inntekt+for+SOS+Barnebyer.png",
        "",
        "Når Hedvig har betalt alle skader for året, går din del av overskuddet til å gi flere barn en trygg oppvekst"
    )
    private val swedishOptions = mapOf(
        option3.id to option3,
        option2.id to option2
    )
    private val norwegianOptions = mapOf(
        norwayOption.id to norwayOption
    )
}
