package com.hedvig.memberservice.web.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = UnderwriterStartSignSessionRequest.SwedishBankId::class, name = "SwedishBankId"),
    JsonSubTypes.Type(value = UnderwriterStartSignSessionRequest.BankIdRedirect::class, name = "BankIdRedirect"),
    JsonSubTypes.Type(value = UnderwriterStartSignSessionRequest.SimpleSign::class, name = "SimpleSign")
)
sealed class UnderwriterStartSignSessionRequest {

    abstract val underwriterSessionReference: UUID

    data class SwedishBankId(
        override val underwriterSessionReference: UUID,
        val nationalIdentification: NationalIdentification,
        val ipAddress: String,
        val isSwitching: Boolean
    ) : UnderwriterStartSignSessionRequest()

    data class BankIdRedirect(
        override val underwriterSessionReference: UUID,
        val nationalIdentification: NationalIdentification,
        val successUrl: String,
        val failUrl: String,
        val country: RedirectCountry
    ) : UnderwriterStartSignSessionRequest()

    data class SimpleSign(
        override val underwriterSessionReference: UUID,
        val nationalIdentification: NationalIdentification
    ) : UnderwriterStartSignSessionRequest()
}


