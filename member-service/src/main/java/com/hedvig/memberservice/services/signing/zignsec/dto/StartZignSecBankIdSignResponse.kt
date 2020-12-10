package com.hedvig.memberservice.services.signing.zignsec.dto

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse

data class StartZignSecBankIdSignResponse(
    val redirectUrl: String?,
    val internalErrorMessage: String? = null,
    val errorMessages: List<ZignSecAuthenticationResponseError>? = null
)

fun StartZignSecBankIdSignResponse.toUnderwriterStartSignSessionResponse() = UnderwriterStartSignSessionResponse.BankIdRedirect(
    this.redirectUrl,
    this.internalErrorMessage,
    this.errorMessages
)
