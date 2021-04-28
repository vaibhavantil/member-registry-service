package com.hedvig.memberservice.events

import com.neovisionaries.i18n.CountryCode
import java.util.UUID

data class MemberSimpleSignedEvent(
    val memberId: Long,
    val nationalIdentification: String,
    val nationality: Nationality,
    val referenceId: UUID
) {
    enum class Nationality(val countryCode: CountryCode) {
        SWEDEN(CountryCode.SE),
        NORWAY(CountryCode.NO),
        DENMARK(CountryCode.DK)
    }
}
