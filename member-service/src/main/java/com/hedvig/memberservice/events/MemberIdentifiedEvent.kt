package com.hedvig.memberservice.events

import java.lang.IllegalArgumentException

data class MemberIdentifiedEvent(
    val memberId: Long,
    val nationalIdentification: NationalIdentification,
    val identificationMethod: IdentificationMethod,
    val firstName: String?,
    val lastName: String?
) {
    data class NationalIdentification(
        val identification: String,
        val nationality: Nationality
    )

    enum class Nationality(val countryCode: String) {
        SWEDEN("SE"),
        NORWAY("NO"),
        DENMARK("DK");

        companion object {
            fun fromCountryCode(countryCode: String): Nationality = values().first { it.countryCode == countryCode }
        }
    }

    enum class IdentificationMethod {
        SWEDISH_BANK_ID,
        NORWEGIAN_BANK_ID,
        DANISH_BANK_ID;

        companion object {
            fun fromIdProviderName(name: String) = when (name) {
                "BankIDSE" -> SWEDISH_BANK_ID
                "BankIDNO" -> NORWEGIAN_BANK_ID
                "NemID" -> DANISH_BANK_ID
                else -> throw IllegalArgumentException("Unknown ID provider type: $name")
            }
        }
    }
}
