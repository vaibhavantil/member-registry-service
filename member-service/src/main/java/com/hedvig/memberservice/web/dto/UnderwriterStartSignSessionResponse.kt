package com.hedvig.memberservice.web.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = UnderwriterStartSignSessionResponse.SwedishBankId::class, name = "SwedishBankId"),
    JsonSubTypes.Type(value = UnderwriterStartSignSessionResponse.BankIdRedirect::class, name = "BankIdRedirect"),
    JsonSubTypes.Type(value = UnderwriterStartSignSessionResponse.SimpleSign::class, name = "SimpleSign")
)
sealed class UnderwriterStartSignSessionResponse {

    abstract val internalErrorMessage: String?

    data class SwedishBankId(
        val autoStartToken: String?,
        override val internalErrorMessage: String? = null
    ) : UnderwriterStartSignSessionResponse()

    data class BankIdRedirect(
        val redirectUrl: String?,
        override val internalErrorMessage: String? = null,
        val errorMessages: List<ZignSecAuthenticationResponseError>? = null
    ) : UnderwriterStartSignSessionResponse()

    data class SimpleSign(
        val successfullyStarted: Boolean,
        override val internalErrorMessage: String? = null
    ) : UnderwriterStartSignSessionResponse()
}
