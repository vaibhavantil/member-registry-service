package com.hedvig.memberservice.events

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

    enum class Nationality {
        SWEDEN,
        NORWAY,
        DENMARK
    }

    enum class IdentificationMethod {
        NORWEGIAN_BANK_ID,
        DANISH_BANK_ID
    }
}
