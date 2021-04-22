package com.hedvig.memberservice.events

import com.neovisionaries.i18n.CountryCode

data class MemberIdentifiedEvent(
    val memberId: Long,
    val nationalIdentification: NationalIdentification,
    val identificationMethod: String,
    val firstName: String?,
    val lastName: String?
) {
    data class NationalIdentification(
        val identification: String,
        val nationality: Nationality
    )

    enum class Nationality(val countryCode: CountryCode) {
        SWEDEN(CountryCode.SE),
        NORWAY(CountryCode.NO),
        DENMARK(CountryCode.DK);

        companion object {
            fun fromCountryCode(countryCode: CountryCode): Nationality = values().first { it.countryCode == countryCode }
        }
    }
}
