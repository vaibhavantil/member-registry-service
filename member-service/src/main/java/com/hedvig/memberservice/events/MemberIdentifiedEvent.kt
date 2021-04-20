package com.hedvig.memberservice.events

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

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

    data class IdentificationMethod @JsonCreator constructor(
        @JsonValue
        val identifier: String
    )
}
